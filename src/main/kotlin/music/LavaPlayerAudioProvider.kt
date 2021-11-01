package music

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import discord4j.voice.AudioProvider
import java.nio.Buffer
import java.nio.ByteBuffer

class LavaPlayerAudioProvider(private val player: AudioPlayer) :
    AudioProvider(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize())) {
    private val frame: MutableAudioFrame = MutableAudioFrame()

    init {
        frame.setBuffer(buffer)
    }

    override fun provide(): Boolean {
        val didProvide: Boolean = player.provide(frame)

        if (didProvide) {
            (buffer as Buffer).flip()
        }
        return didProvide
    }
}