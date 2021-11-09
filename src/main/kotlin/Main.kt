package com.shstrkr

import CommandBinding
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import commands.music.MusicCommandManager
import executeBf
import reactor.core.publisher.Mono
import java.io.File


fun main() {
    val discordClient: DiscordClient = DiscordClient.create(File("key").readLines().first())
    val client: GatewayDiscordClient = discordClient.login().block() ?: throw Exception("Couldnt login!")

    val musicCommandManager = MusicCommandManager()

    val commands = listOf(
        CommandBinding(::executeBf),
        CommandBinding(musicCommandManager::join),
        CommandBinding(musicCommandManager::play),
        CommandBinding(musicCommandManager::skip),
        CommandBinding(musicCommandManager::queue),
        CommandBinding(musicCommandManager::playing),
        CommandBinding(musicCommandManager::remove),
    )

    val applicationId = client.restClient.applicationId.block() ?: throw Exception("Couldnt get app id!")
    val guildsIds = listOf(239604599701504010, 155061423016247296)

    for (guildId in guildsIds) {
        /*
        val applicationCommands = client.restClient.applicationService.getGuildApplicationCommands(applicationId, guildId).collectList().block()!!
        for (applicationCommand in applicationCommands) {
            client.restClient.applicationService.deleteGuildApplicationCommand(
                applicationId,
                guildId,
                applicationCommand.id().toLong()
            )
        }
         */

        for (command in commands) {
            client.restClient.applicationService
                .createGuildApplicationCommand(applicationId, guildId, command.applicationCommand)
                .subscribe()
            println("$guildId - registered ${command.applicationCommand.name()}")
        }
    }

    val commandMap = commands.associateBy { it.applicationCommand.name() }

    client.on(ChatInputInteractionEvent::class.java) { event ->
            val command = commandMap[event.commandName] ?: throw Exception("Command name was not mapped!")
            return@on command.execute(event)
        }.doOnError { e -> error("Error occurred in command execution $e") }
        .subscribe()

    client.on(VoiceStateUpdateEvent::class.java) { event ->
        if (event.isLeaveEvent && event.current.userId == client.selfId) {
            musicCommandManager.musicManager.stop()
        }
        return@on Mono.empty<Void>()
    }.subscribe()

    client.onDisconnect().block()
}