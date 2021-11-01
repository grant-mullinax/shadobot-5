import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec
import discord4j.discordjson.json.*
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import kotlin.reflect.*
import kotlin.reflect.full.createType

annotation class ApplicationCommand(val name: String, val description: String)
annotation class ApplicationOption(val name: String, val description: String)

interface ParameterData {
    val kParameter: KParameter
}
class ExplicitParameter(val name: String, override val kParameter: KParameter) : ParameterData
class ImplicitParameter(override val kParameter: KParameter) : ParameterData

class CommandBinding(private val function: KFunction<*>) {
    val applicationCommand: ImmutableApplicationCommandRequest
    private val executionInfo: List<ParameterData>

    init {
        val functionAnnotation = this.function.annotations.filterIsInstance<ApplicationCommand>().firstOrNull()
            ?: throw Exception("Function was not annotated")
        var applicationCommandBuilder =
            ApplicationCommandRequest.builder()
                .name(functionAnnotation.name)
                .description(functionAnnotation.description)

        val executionInfo = mutableListOf<ParameterData>()
        for (parameter in function.parameters) {

            // todo fix ugly
            if (parameter.type == typeFromClass(ChatInputInteractionEvent::class)) {
                executionInfo.add(ImplicitParameter(parameter))
                continue
            }

            val optionAnnotation = parameter.annotations.filterIsInstance<ApplicationOption>().firstOrNull()
                ?: throw Exception("Parameter was not annotated")
            val applicationCommandOption = ApplicationCommandOptionData.builder()
                .name(optionAnnotation.name)
                .description(optionAnnotation.description)
                .type(
                    // todo look at typeOf<Int>()
                    when(parameter.type) {
                        typeFromClass(String::class) -> ApplicationCommandOption.Type.STRING.value
                        typeFromClass(Int::class) -> ApplicationCommandOption.Type.INTEGER.value
                        typeFromClass(Boolean::class) -> ApplicationCommandOption.Type.BOOLEAN.value
                        typeFromClass(Double::class) -> ApplicationCommandOption.Type.NUMBER.value
                        typeFromClass(User::class) -> ApplicationCommandOption.Type.USER.value
                        typeFromClass(Channel::class) -> ApplicationCommandOption.Type.CHANNEL.value
                        typeFromClass(Role::class) -> ApplicationCommandOption.Type.ROLE.value
                        else -> throw Exception("Type was not mapped for type ${parameter.type}")
                    })
                .required(!parameter.isOptional)
                .build()
            applicationCommandBuilder = applicationCommandBuilder.addOption(applicationCommandOption)

            executionInfo.add(ExplicitParameter(optionAnnotation.name, parameter))
        }
        this.executionInfo = executionInfo.toList()
        applicationCommand = applicationCommandBuilder.build()
    }

    @ExperimentalStdlibApi
    fun execute(event: ChatInputInteractionEvent): Mono<Void> {
        val parameters: MutableMap<KParameter, Any> = mutableMapOf()
        for (parameter in executionInfo) {
            when (parameter) {
                is ExplicitParameter -> {
                    val optionalOptionValue = event.getOption(parameter.name)
                    if (!optionalOptionValue.isPresent && parameter.kParameter.isOptional) {
                        // its optional and the user didnt include it- pass
                        continue
                    }
                        val optionValue = optionalOptionValue.get().value.get()
                    parameters[parameter.kParameter] = when (parameter.kParameter.type) {
                        typeFromClass(String::class) -> optionValue.asString()
                        typeFromClass(Int::class) -> optionValue.asLong()
                        typeFromClass(Boolean::class) -> optionValue.asBoolean()
                        typeFromClass(Double::class) -> optionValue.asDouble()
                        typeFromClass(User::class) -> optionValue.asUser().block()!!
                        typeFromClass(Channel::class) -> optionValue.asChannel().block()!!
                        typeFromClass(Role::class) -> optionValue.asRole().block()!!
                        else -> throw Exception("Type was not mapped in execute!")
                    }
                }
                is ImplicitParameter -> {
                    parameters[parameter.kParameter] = when (parameter.kParameter.type) {
                        typeFromClass(ChatInputInteractionEvent::class) -> event
                        else -> throw Exception("Type was not mapped in execute!")
                    }
                }
            }
        }

        return when (function.returnType) {
            typeFromClass(String::class) ->
                event.reply(function.callBy(parameters) as String)
            typeFromClass(InteractionApplicationCommandCallbackSpec::class) ->
                event.reply(function.callBy(parameters) as InteractionApplicationCommandCallbackSpec)
            typeOf<Mono<Void>>() -> function.callBy(parameters) as Mono<Void>
            else -> throw Exception("Function return type was not mapped in execute!")
        }
    }

    companion object {
        private val classToTypeMap = mutableMapOf<KClass<*>, KType>()

        fun typeFromClass(cls: KClass<*>): KType {
            if (!classToTypeMap.containsKey(cls)) {
                classToTypeMap[cls] = cls.createType()
            }
            return classToTypeMap[cls]!!
        }
    }
}