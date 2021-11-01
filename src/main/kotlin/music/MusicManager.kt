package music

import ApplicationCommand
import ApplicationOption
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.command.Interaction
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.VoiceChannelJoinSpec
import discord4j.voice.AudioProvider
import discord4j.voice.VoiceConnection
import reactor.core.publisher.Flux
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

    companion object {
        val singleLink: Regex = Regex("^https://(www|m|music)\\.youtube\\.com/watch\\?v=[a-zA-Z0-9_-]{11}$")
        val singleDirectLink: Regex = Regex("^https://youtu\\.be/[a-zA-Z0-9_-]{11}$")
        val playlistLink: Regex =
            Regex("^https://(www|m|music)\\.youtube\\.com/(watch\\?v=[a-zA-Z0-9_-]{11}&list=|playlist\\?list=)(PL|LL|FL|UU)[a-zA-Z0-9_-]+.*$")
    }

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
        @ApplicationOption("query", "song query")
        query: String
    ): Mono<Void> {
        return Mono.justOrEmpty(event.interaction.member)
            .flatMap(Member::getVoiceState)
            .flatMap(VoiceState::getChannel)
            .zipWith(
                event.interaction.guild
                    .flatMap(Guild::getVoiceConnection)
                    .flatMap(VoiceConnection::getChannelId)
                    .map { Optional.of(it) }
                    .switchIfEmpty(Mono.just(Optional.empty()))
            )
            .flatMap { tuple ->
                if (tuple.t2.isPresent && tuple.t1.id == tuple.t2.get()) {
                    return@flatMap Mono.empty()
                } else {
                    return@flatMap tuple.t1.join(VoiceChannelJoinSpec.create().withProvider(this.provider))
                }
            }
            .then(musicQuery(query))
            .doOnNext { player.playTrack(it) }
            .flatMap { track -> event.reply("Playing ${track.info.title} (${track.info.uri})") }
    }

    @ApplicationCommand("skip", "skip song")
    fun skip(
        event: ChatInputInteractionEvent,
    ): Mono<Void> {
        return Mono.fromCallable { player.stopTrack() }.then(event.reply("Skipped song"))
    }

    private fun musicQuery(
        query: String,
    ): Mono<AudioTrack> {
        val qualifiedQuery = if (singleLink.matches(query) || singleDirectLink.matches(query) || playlistLink.matches(query)) {
            query
        } else {
            "ytsearch:$query"
        }

        return Mono.create { emitter ->
            playerManager.loadItem(
                qualifiedQuery,
                object : AudioLoadResultHandler {
                    override fun loadFailed(exception: FriendlyException) {
                        emitter.error(exception)
                    }

                    override fun trackLoaded(track: AudioTrack) {
                        emitter.success(track)
                    }

                    override fun noMatches() {
                        emitter.success()
                    }

                    override fun playlistLoaded(playlist: AudioPlaylist) {
                        emitter.success(playlist.tracks[0])
                    }
                }
            )
        }
    }
}