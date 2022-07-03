package com.shadobot

import com.shadobot.commands.executeBf
import com.shadobot.commands.music.MusicCommandManager
import com.shadobot.commands.ping
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.io.File

fun main() {
    val logger: Logger = LoggerFactory.getLogger("main")


    val discordClient: DiscordClient = DiscordClient.create(File("key").readLines().first())
    val client: GatewayDiscordClient = discordClient.login().block() ?: throw Exception("Couldnt login!")

    val chatInputHandler = ChatInputHandlerBuilder()
        .add(::executeBf)
        .add(::ping)
        .addModule(MusicCommandManager::class)
        .build()

    chatInputHandler.createApplicationCommands(client)

    client.on(ChatInputInteractionEvent::class.java) { event ->
        logger.info(event.commandName)
        return@on chatInputHandler.handle(event)
    }.doOnError { e -> logger.error("Error occurred in command execution $e") }.subscribe()

    client.on(VoiceStateUpdateEvent::class.java) { event ->
        if (event.isLeaveEvent && event.current.userId == client.selfId) {
            // musicCommandManager.musicManager.stop()
        }
        return@on Mono.empty<Void>()
    }.subscribe()

    client.onDisconnect().block()
}
