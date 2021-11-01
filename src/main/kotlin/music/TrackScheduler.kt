package music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class TrackScheduler(private val player: AudioPlayer): AudioLoadResultHandler {
    override fun trackLoaded(track: AudioTrack?) {
        player.playTrack(track)
    }

    override fun playlistLoaded(playlist: AudioPlaylist?) {
        player.playTrack(playlist!!.tracks.first())
    }

    override fun noMatches() {
        TODO("Not yet implemented")
    }

    override fun loadFailed(exception: FriendlyException?) {
        TODO("Not yet implemented")
    }
}