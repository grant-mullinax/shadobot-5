package binding.optiondata.types

import binding.optiondata.abstracts.AbstractParameterData
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlin.reflect.KParameter

class ChatInputInteractionEventParameterData(parameter: KParameter): AbstractParameterData(parameter) {
    override fun extractValueFromEvent(event: ChatInputInteractionEvent): ChatInputInteractionEvent {
        return event
    }
}