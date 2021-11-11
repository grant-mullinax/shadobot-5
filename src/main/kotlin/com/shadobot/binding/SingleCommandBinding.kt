package com.shadobot.binding

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Mono
import kotlin.reflect.KFunction

class SingleCommandBinding(private val function: KFunction<Mono<Void>>) : AbstractCommandBinding(function) {
    fun execute(event: ChatInputInteractionEvent): Mono<Void> {
        return function.callBy(getParameterMapFromEvent(event))
    }
}
