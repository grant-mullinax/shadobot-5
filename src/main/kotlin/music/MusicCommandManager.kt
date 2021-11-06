package music

import binding.ApplicationCommand
import binding.ApplicationOption
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.VoiceChannelJoinSpec
import discord4j.voice.VoiceConnection
import music.guts.MusicManager
import reactor.core.publisher.Mono
import java.util.*


class MusicCommandManager {
    val musicManager: MusicManager = MusicManager()

    @ApplicationCommand("join", "joins vc")
    fun join(event: ChatInputInteractionEvent): Mono<Void> {
        return Mono.justOrEmpty(event.interaction.member)
            .flatMap { obj: Member -> obj.voiceState }
            .flatMap(VoiceState::getChannel)
            .flatMap { channel -> channel.join(VoiceChannelJoinSpec.create().withProvider(this.musicManager.provider)) }
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
                    .map { channelId -> Optional.of(channelId) }
                    .switchIfEmpty(Mono.just(Optional.empty()))
            )
            .doOnNext { event.reply("Loading music...").subscribe() }
            .flatMap { tuple ->
                if (tuple.t2.isPresent && tuple.t1.id == tuple.t2.get()) {
                    return@flatMap Mono.empty()
                } else {
                    return@flatMap tuple.t1.join(VoiceChannelJoinSpec.create().withProvider(this.musicManager.provider))
                }
            }
            .then(musicManager.query(query))
            .doOnNext { track -> musicManager.queueTrack(track) }
            .flatMap { track -> event.editReply("Playing ${track.info.title} (${track.info.uri})") }
            .switchIfEmpty(event.editReply("No results found!"))
            .then()
    }

    @ApplicationCommand("skip", "skip song")
    fun skip(
        event: ChatInputInteractionEvent,
    ): Mono<Void> {
        return Mono.fromCallable { musicManager.stop() }.then(event.reply("Skipped song"))
    }

    @ApplicationCommand("queue", "check the queue")
    fun queue(
        event: ChatInputInteractionEvent,
    ): Mono<Void> {
        return if (musicManager.queue.isNotEmpty()) {
            event.reply("Playing ${musicManager.playing!!.info.title} (<${musicManager.playing!!.info.uri}>)\n\n"+
                    musicManager.queue.withIndex()
                        .map { (i, track) -> "$i - ${track.info.title} (<${track.info.uri}>)" }
                        .reduce { acc, s -> "$acc\n$s" }
            )
        } else {
            event.reply("Queue is empty")
        }
    }

    @ApplicationCommand("remove", "remove index from queue")
    fun remove(
        event: ChatInputInteractionEvent,
        @ApplicationOption("index", "song index to remove")
        index: Int = 0
    ): Mono<Void> {
        return if (index < musicManager.queue.size && index >= 0) {
            val track = musicManager.queue.removeAt(index)
            event.reply("Removed ${track.info.title}")
        } else {
            event.reply("Index out of range")
        }
    }

    @ApplicationCommand("playing", "what is playing")
    fun playing(
        event: ChatInputInteractionEvent,
    ): Mono<Void> {
        return if (musicManager.playing == null) {
            event.reply("Playing ${musicManager.playing!!.info.title} (${musicManager.playing!!.info.uri})")
        } else {
            event.reply("Nothing is playing")
        }
    }
}
