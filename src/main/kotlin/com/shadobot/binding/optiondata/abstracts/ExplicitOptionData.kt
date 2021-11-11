package com.shadobot.binding.optiondata.abstracts

import com.shadobot.binding.ApplicationOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlin.reflect.KParameter

abstract class ExplicitOptionData(parameter: KParameter) : AbstractParameterData(parameter) {
    abstract val commandOptionType: Int
    val optionAnnotation = parameter.annotations.filterIsInstance<ApplicationOption>().firstOrNull()
        ?: throw Exception("Parameter was not annotated")

    // todo right now we assume that all results from discord are non-null for all non-null options
    // this might not be true. but it probably is
    internal fun getOptionValueFromEvent(event: ChatInputInteractionEvent): ApplicationCommandInteractionOptionValue? {
        val optionalOptionValue = event.getOption(optionAnnotation.name)
        if (!optionalOptionValue.isPresent) {
            // this should only happen if the option is optional hopefully
            return null
        }

        // value will be null if we use suboptions... but we dont.
        return optionalOptionValue.get().value.get()
    }
}