import binding.*
import binding.optiondata.abstracts.AbstractParameterData
import binding.optiondata.abstracts.ExplicitOptionData
import binding.optiondata.types.*
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.discordjson.json.*
import reactor.core.publisher.Mono
import kotlin.reflect.*

class CommandBinding(private val function: KFunction<Mono<Void>>) {
    val applicationCommand: ImmutableApplicationCommandRequest
    private val parameterDataList: List<AbstractParameterData>

    companion object {
        private fun mapParameterToParameterData(parameter: KParameter): AbstractParameterData {
            return when(parameter.type.classifier) {
                ChatInputInteractionEvent::class -> ChatInputInteractionEventParameterData(parameter)

                String::class -> StringOptionData(parameter)
                Int::class -> IntOptionData(parameter)
                Boolean::class -> BooleanOptionData(parameter)
                Double::class -> DoubleOptionData(parameter)
                Mono::class -> {
                    when (parameter.type.arguments.first().type!!.classifier) {
                        User::class -> UserOptionData(parameter)
                        Channel::class -> ChannelOptionData(parameter)
                        Role::class -> RoleOptionData(parameter)
                        else -> throw Exception("Type argument of mono was not mapped")
                    }
                }
                else -> throw Exception("Type was not mapped for type ${parameter.type}")
            }
        }
    }

    init {
        val functionAnnotation = this.function.annotations.filterIsInstance<ApplicationCommand>().firstOrNull()
            ?: throw Exception("Function was not annotated")
        var applicationCommandBuilder =
            ApplicationCommandRequest.builder()
                .name(functionAnnotation.name)
                .description(functionAnnotation.description)

        val tempNameToParameterDataMap = mutableListOf<AbstractParameterData>()

        for (parameter in function.parameters) {
            val parameterData = mapParameterToParameterData(parameter)

            tempNameToParameterDataMap.add(parameterData)

            if (parameterData is ExplicitOptionData) {
                val applicationCommandOption = ApplicationCommandOptionData.builder()
                    .name(parameterData.optionAnnotation.name)
                    .description(parameterData.optionAnnotation.description)
                    .type(parameterData.commandOptionType)
                    .required(!parameter.isOptional)
                    .build()
                applicationCommandBuilder = applicationCommandBuilder.addOption(applicationCommandOption)
            }
        }

        this.applicationCommand = applicationCommandBuilder.build()
        this.parameterDataList = tempNameToParameterDataMap
    }

    fun execute(event: ChatInputInteractionEvent): Mono<Void> {
        return function.callBy(
            this.parameterDataList
                .map { parameterData -> parameterData.parameter to parameterData.extractValueFromEvent(event) }
                .filter { it.second != null }
                .toMap()
        )
    }
}
