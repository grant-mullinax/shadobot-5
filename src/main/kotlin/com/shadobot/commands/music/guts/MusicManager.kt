package com.shadobot.commands.music.guts

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import com.shadobot.commands.music.LavaPlayerAudioProvider
import discord4j.voice.AudioProvider
import reactor.core.publisher.Mono

class MusicManager : AudioEventListener {
    val provider: AudioProvider
    private val player: AudioPlayer
    private val playerManager: DefaultAudioPlayerManager = DefaultAudioPlayerManager()

    val queue = mutableListOf<AudioTrack>()
    var playing: AudioTrack? = null

    companion object {
        val singleLink: Regex = Regex("^https://(www|m|commands.music)\\.youtube\\.com/watch\\?v=[a-zA-Z0-9_-]{11}$")
        val singleDirectLink: Regex = Regex("^https://youtu\\.be/[a-zA-Z0-9_-]{11}$")
        val playlistLink: Regex =
            Regex("^https://(www|m|commands.music)\\.youtube\\.com/(watch\\?v=[a-zA-Z0-9_-]{11}&list=|playlist\\?list=)(PL|LL|FL|UU)[a-zA-Z0-9_-]+.*$")
    }

    init {
        playerManager.configuration.frameBufferFactory =
            AudioFrameBufferFactory { bufferDuration, format, stopping ->
                NonAllocatingAudioFrameBuffer(
                    bufferDuration,
                    format,
                    stopping
                )
            }
        AudioSourceManagers.registerRemoteSources(playerManager)

        this.player = playerManager.createPlayer()
        this.player.addListener(this)
        this.provider = LavaPlayerAudioProvider(this.player)
    }

    fun query(
        query: String,
    ): Mono<AudioTrack> {
        val qualifiedQuery =
            if (singleLink.matches(query) || singleDirectLink.matches(query) || playlistLink.matches(query)) {
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

    fun queueTrack(track: AudioTrack) {
        if (playing == null) {
            this.play(track)
        } else {
            this.queue.add(track)
        }
    }

    private fun play(track: AudioTrack) {
        this.playing = track
        this.player.playTrack(track)
    }

    private fun playNextInQueue() {
        if (queue.isNotEmpty()) {
            val track = queue.removeFirst()
            this.play(track)
        }
    }

    fun stop() {
        this.player.stopTrack()
        this.playing = null
    }

    override fun onEvent(event: AudioEvent?) {
        when (event) {
            is PlayerPauseEvent -> {
            }
            is PlayerResumeEvent -> {
            }
            is TrackEndEvent -> {
                this.playNextInQueue()
            }
            is TrackExceptionEvent -> {
                print("Track exception happened")
                this.playNextInQueue()
            }
            is TrackStartEvent -> {
            }
            is TrackStuckEvent -> {
                print("Track got stuck for some reason?")
                this.playNextInQueue()
            }
            null -> {
                print("AudioEvent was null?")
            }
        }
    }
}