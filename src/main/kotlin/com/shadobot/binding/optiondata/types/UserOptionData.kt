package com.shadobot.binding.optiondata.types

import com.shadobot.binding.optiondata.abstracts.ExplicitOptionData
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Mono
import kotlin.reflect.KParameter

class UserOptionData(parameter: KParameter) : ExplicitOptionData(parameter) {
    override val commandOptionType = ApplicationCommandOption.Type.USER.value

    override fun extractValueFromEvent(event: ChatInputInteractionEvent): Mono<User>? {
        return getOptionValueFromEvent(event)?.asUser()
    }
}