package com.shadobot

import com.shadobot.binding.AbstractCommandBinding
import com.shadobot.binding.ApplicationCommand
import com.shadobot.binding.ModuleCommandBinding
import com.shadobot.binding.SingleCommandBinding
import reactor.core.publisher.Mono
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

class ChatInputHandlerBuilder {
    private val commandBindings = mutableListOf<AbstractCommandBinding>()

    fun add(function: KFunction<Mono<Void>>): ChatInputHandlerBuilder {
        commandBindings.add(SingleCommandBinding(function))

        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun addModule(kClass: KClass<*>): ChatInputHandlerBuilder {
        for (function in kClass.functions) {
            if (function.hasAnnotation<ApplicationCommand>()) {
                // unchecked cast! u gotta believe that no one is annotating stuff that doesnt return mono
                commandBindings.add(ModuleCommandBinding(function as KFunction<Mono<Void>>))
            }
        }

        return this
    }

    fun build(): ChatInputHandler {
        return ChatInputHandler(commandBindings.associateBy { it.applicationCommand.name() })
    }
}