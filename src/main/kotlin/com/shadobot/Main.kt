package com.shadobot

import com.shadobot.commands.executeBf
import com.shadobot.commands.music.MusicCommandManager
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Mono
import java.io.File


fun main() {
    val discordClient: DiscordClient = DiscordClient.create(File("key").readLines().first())
    val client: GatewayDiscordClient = discordClient.login().block() ?: throw Exception("Couldnt login!")

    val chatInputHandler = ChatInputHandlerBuilder()
        .add(::executeBf)
        .addModule(MusicCommandManager::class)
        .build()

    chatInputHandler.createApplicationCommands(client)

    client.on(ChatInputInteractionEvent::class.java) { event ->
        return@on chatInputHandler.handle(event)
    }
        .doOnError { e -> error("Error occurred in command execution $e") }
        .subscribe()

    client.on(VoiceStateUpdateEvent::class.java) { event ->
        if (event.isLeaveEvent && event.current.userId == client.selfId) {
            // musicCommandManager.musicManager.stop()
        }
        return@on Mono.empty<Void>()
    }.subscribe()

    client.onDisconnect().block()
}