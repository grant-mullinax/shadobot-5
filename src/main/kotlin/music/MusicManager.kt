package music

import ApplicationCommand
import ApplicationOption
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.command.Interaction
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.VoiceChannelJoinSpec
import discord4j.voice.AudioProvider
import reactor.core.publisher.Mono
import sun.audio.AudioPlayer.player
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean


class MusicManager {
    private val provider: AudioProvider
    private val player: AudioPlayer
    private val playerManager: DefaultAudioPlayerManager = DefaultAudioPlayerManager()
    private val scheduler: TrackScheduler
    // private val joinedVoiceChannel: VoiceChannel? = Null

    init {

        playerManager.configuration.frameBufferFactory =
            AudioFrameBufferFactory { bufferDuration: Int, format: AudioDataFormat?, stopping: AtomicBoolean? ->
                NonAllocatingAudioFrameBuffer(
                    bufferDuration,
                    format,
                    stopping
                )
            }
        AudioSourceManagers.registerRemoteSources(playerManager)

        this.player = playerManager.createPlayer()
        this.provider = LavaPlayerAudioProvider(this.player)

        this.scheduler = TrackScheduler(this.player)
    }

    @ApplicationCommand("join", "joins vc")
    fun join(event: ChatInputInteractionEvent): Mono<Void> {
        return Mono.justOrEmpty(event.interaction.member)
            .flatMap { obj: Member -> obj.voiceState }
            .flatMap(VoiceState::getChannel)
            .flatMap { channel -> channel.join(VoiceChannelJoinSpec.create().withProvider(this.provider)) }
            .then(event.reply("Joined channel"))
    }

    @ApplicationCommand("play", "play song")
    fun play(
        event: ChatInputInteractionEvent,
        @ApplicationOption("url", "song url")
        title: String
    ): Mono<Void> {
        return event.interaction.guild
            .flatMap(Guild::getVoiceConnection)
            Mono.justOrEmpty(event.interaction.member)
                .flatMap { obj: Member -> obj.voiceState }
                .flatMap(VoiceState::getChannel)
            .then(event.reply("lol"))
    }
}