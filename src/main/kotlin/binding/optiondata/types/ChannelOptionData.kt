package binding.optiondata.types

import binding.optiondata.abstracts.ExplicitOptionData
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Mono
import kotlin.reflect.KParameter

class ChannelOptionData(parameter: KParameter) : ExplicitOptionData(parameter) {
    override val commandOptionType = ApplicationCommandOption.Type.CHANNEL.value

    override fun extractValueFromEvent(event: ChatInputInteractionEvent): Mono<Channel>? {
        return getOptionValueFromEvent(event)?.asChannel()
    }
}