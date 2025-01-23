package dev.shaper.rypolixy.utils.musicplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.shaper.rypolixy.utils.musicplayer.lavaplayer.LavaPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack as LavaTrack

class AudioTrack (val audioTrack: LavaTrack){

    enum class PlayStatus{
        IDLE,PLAYING,END,REMOVED
    }
    var status = PlayStatus.IDLE

    fun playWith(player: AudioPlayer) {
        status = PlayStatus.PLAYING
        player.playTrack(audioTrack)
    }

    fun clone() = AudioTrack(audioTrack.makeClone())

    fun seek(millis: Long) {
        val newPosition = audioTrack.position + millis
        audioTrack.position = newPosition.coerceIn(0..audioTrack.duration)
    }

    fun startOver() {
        audioTrack.position = 0
    }

    companion object {

        class TrackLoader(val query: String, private val lambda: suspend () -> AudioTrack?) {
            suspend fun invoke() = lambda.invoke()
        }

        private fun LavaTrack.toTrack() = AudioTrack(this)

        suspend fun trackLoader(query: String) = TrackLoader(query) {
            LavaPlayerManager.loadTrack(query)?.toTrack()
        }

        suspend fun playlistLoader(query: String): List<AudioTrack> {
            return LavaPlayerManager.loadPlaylist(query).map {
                it.toTrack()
            }
        }
    }

}