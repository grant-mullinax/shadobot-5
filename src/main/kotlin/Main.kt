import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.VoiceServerUpdateEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import music.MusicManager
import java.io.File


@OptIn(ExperimentalStdlibApi::class)
fun main() {
    val discordClient: DiscordClient = DiscordClient.create(File("key").readLines().first())
    val client: GatewayDiscordClient = discordClient.login().block() ?: throw Exception("Couldnt login!")

    val applicationId = client.restClient.applicationId.block() ?: throw Exception("Couldnt get app id!")

    val musicManager = MusicManager()

    val commands = listOf(
        CommandBinding(::testCommand),
        CommandBinding(::executeBf),
        CommandBinding(musicManager::join),
        CommandBinding(musicManager::play),
        CommandBinding(musicManager::skip),
    )

    for (command in commands) {
        client.restClient.applicationService
            .createGuildApplicationCommand(applicationId, 239604599701504010, command.applicationCommand)
            .subscribe()

        client.restClient.applicationService
            .createGuildApplicationCommand(applicationId, 155061423016247296, command.applicationCommand)
            .subscribe()
    }

    val commandMap = commands.associateBy { it.applicationCommand.name() }

    client.on(ChatInputInteractionEvent::class.java) { event ->
            val command = commandMap[event.commandName] ?: throw Exception("Command name was not mapped!")
            return@on command.execute(event)
        }
        .doOnError { e -> error("Error occurred in command execution $e") }
        .subscribe()

    client.onDisconnect().block()
}