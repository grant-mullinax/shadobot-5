package com.shadobot.binding.optiondata.types

import com.shadobot.binding.optiondata.abstracts.ExplicitOptionData
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlin.reflect.KParameter

class BooleanOptionData(parameter: KParameter) : ExplicitOptionData(parameter) {
    override val commandOptionType = ApplicationCommandOption.Type.BOOLEAN.value

    override fun extractValueFromEvent(event: ChatInputInteractionEvent): Boolean? {
        return getOptionValueFromEvent(event)?.asBoolean()
    }
}