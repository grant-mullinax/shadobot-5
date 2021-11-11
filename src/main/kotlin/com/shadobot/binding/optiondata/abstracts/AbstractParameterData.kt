package com.shadobot.binding.optiondata.abstracts

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlin.reflect.KParameter

abstract class AbstractParameterData(val parameter: KParameter) {
    abstract fun extractValueFromEvent(event: ChatInputInteractionEvent): Any?
}