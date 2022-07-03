package com.shadobot.commands

import com.shadobot.binding.ApplicationCommand
import com.shadobot.binding.ApplicationOption
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono

@ApplicationCommand("ping", "its the test")
fun ping(event: ChatInputInteractionEvent): InteractionApplicationCommandCallbackReplyMono {
    return event.reply("pong")
}