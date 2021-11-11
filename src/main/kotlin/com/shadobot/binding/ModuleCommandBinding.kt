package com.shadobot.binding

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Mono
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.jvmErasure

class ModuleCommandBinding(private val function: KFunction<Mono<Void>>) : AbstractCommandBinding(function) {
    private val instanceParam = function.instanceParameter
        ?: function.extensionReceiverParameter
        ?: throw IllegalArgumentException("Given command function must not have a instance already bound")
    val moduleKClass = instanceParam.type.jvmErasure

    fun execute(event: ChatInputInteractionEvent, withInstance: Any): Mono<Void> {
        return function.callBy(getParameterMapFromEvent(event) + (instanceParam to withInstance))
    }
}