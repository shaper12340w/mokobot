package dev.shaper.rypolixy.utils.musicplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class MediaBehavior (val audioTrack: AudioTrack){

    enum class PlayStatus{
        IDLE,PLAYING,END,REMOVED
    }
    var status = PlayStatus.IDLE

    fun playWith(player: AudioPlayer) {
        status = PlayStatus.PLAYING
        player.playTrack(audioTrack)
    }

    fun clone() = MediaBehavior(audioTrack.makeClone())

    fun seek(millis: Long) {
        val newPosition = audioTrack.position + millis
        audioTrack.position = newPosition.coerceIn(0..audioTrack.duration)
    }

    fun startOver() {
        audioTrack.position = 0
    }

    companion object {

        class TrackLoader(val query: String, private val lambda: suspend () -> MediaBehavior?) {
            suspend fun invoke() = lambda.invoke()
        }
        fun AudioTrack.toTrack() = MediaBehavior(this)

    }

}