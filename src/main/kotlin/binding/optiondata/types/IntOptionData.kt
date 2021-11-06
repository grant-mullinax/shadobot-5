package binding.optiondata.types

import binding.optiondata.abstracts.ExplicitOptionData
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlin.reflect.KParameter

class IntOptionData(parameter: KParameter) : ExplicitOptionData(parameter) {
    override val commandOptionType = ApplicationCommandOption.Type.INTEGER.value

    override fun extractValueFromEvent(event: ChatInputInteractionEvent): Int? {
        return getOptionValueFromEvent(event)?.asLong()?.toInt()
    }
}