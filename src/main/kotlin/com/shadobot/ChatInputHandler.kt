package com.shadobot

import com.shadobot.binding.AbstractCommandBinding
import com.shadobot.binding.ModuleCommandBinding
import com.shadobot.binding.SingleCommandBinding
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Mono
import kotlin.reflect.KClassifier
import kotlin.reflect.full.primaryConstructor



// todo explicitly making modules inherit, deliver instance-classed kfunctions, and declare their commands might be a good idea
class ChatInputHandler(private val commandMap: Map<String, AbstractCommandBinding>) {
    private val commandPackageInstanceMap = mutableMapOf<Pair<KClassifier, Snowflake>, Any>()

    fun handle(event: ChatInputInteractionEvent): Mono<Void> {
        val commandBinding = commandMap[event.commandName] ?: throw Exception("Command name was not mapped!")
        when (commandBinding) {
            is SingleCommandBinding -> return commandBinding.execute(event)
            is ModuleCommandBinding -> {
                val optionalGuildId = event.interaction.guildId
                if (!optionalGuildId.isPresent) {
                    return event.reply("You cannot use this command outside of a guild!")
                }

                val key = commandBinding.moduleKClass to optionalGuildId.get()
                var instance = commandPackageInstanceMap[key]
                if (instance == null) {
                    instance = commandBinding.moduleKClass.primaryConstructor?.call()
                        ?: throw Exception("Tried to call a module without a primary constructor")
                    commandPackageInstanceMap[key] = instance
                }

                return commandBinding.execute(event, instance)
            }
            else -> throw Exception("Command binding was neither module or single!?")
        }
    }

    private val devGuildIds = listOf(239604599701504010)

    fun createApplicationCommands(client: GatewayDiscordClient) {
        val applicationId = client.restClient.applicationId.block() ?: throw Exception("Couldn't get app id!")

        client.restClient.guilds.flatMap {
            client.guilds.flatMap { guild ->
                client.restClient.applicationService.getGuildApplicationCommands(
                    applicationId,
                    guild.id.asLong()
                ).flatMap { applicationCommmand ->
                    client.restClient.applicationService.deleteGuildApplicationCommand(
                        applicationId,
                        guild.id.asLong(),
                        applicationCommmand.id().toLong()
                    )
                }
            }
        }

        client.restClient.applicationService.getGlobalApplicationCommands(applicationId)
            .flatMap { client.restClient.applicationService.deleteGlobalApplicationCommand(applicationId, it.id().toLong())}

        for (command in commandMap.values) {
            client.restClient.applicationService
                .createGlobalApplicationCommand(applicationId, command.applicationCommand)
                .subscribe()
            println("Registered global ${command.applicationCommand.name()}")
        }

        for (guildId in devGuildIds) {
            for (command in commandMap.values) {
                client.restClient.applicationService
                    .createGlobalApplicationCommand(applicationId, command.applicationCommand)
                    .subscribe()
                println("$guildId - registered ${command.applicationCommand.name()}")
            }
        }
    }
}