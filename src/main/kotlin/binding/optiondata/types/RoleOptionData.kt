package binding.optiondata.types

import binding.optiondata.abstracts.ExplicitOptionData
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Mono
import kotlin.reflect.KParameter

class RoleOptionData(parameter: KParameter) : ExplicitOptionData(parameter) {
    override val commandOptionType = ApplicationCommandOption.Type.ROLE.value

    override fun extractValueFromEvent(event: ChatInputInteractionEvent): Mono<Role>? {
        return getOptionValueFromEvent(event)?.asRole()
    }
}