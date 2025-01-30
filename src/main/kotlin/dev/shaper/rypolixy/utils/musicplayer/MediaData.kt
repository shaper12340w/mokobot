package dev.shaper.rypolixy.utils.musicplayer

import dev.kord.common.annotation.KordVoice
import dev.kord.voice.VoiceConnection
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.kord.voice.AudioProvider

data class MediaData @OptIn(KordVoice::class) constructor(
    val queue:          MutableList<MediaTrack>,
    val player:         AudioPlayer,
    val provider:       AudioProvider,
    val connection:     VoiceConnection,
    val options:        MediaUtils.PlayerOptions,
){
    var index:          Int = 0
    var subIndex:       Int = 0

    var paused:         Boolean = false
    var position:       Long = 0
    var terminated:     Boolean = false

    fun current(): MediaTrack? = queue.getOrNull(index)

    fun currentTrack(): MediaTrack.Track?{
        return when(current()) {
            is MediaTrack.Track     -> queue[index] as MediaTrack.Track
            is MediaTrack.Playlist  -> {
                val trackData =  (queue[index] as MediaTrack.Playlist).tracks[subIndex]
                if(trackData is MediaTrack.Track) trackData
                else null
            }
            else -> null
        }
    }

    fun currentData(): MediaBehavior? {
        return when(val track = currentTrack()){
            is MediaTrack.Track -> track.data
            else                -> null
        }
    }

    fun update() {
        currentTrack()?.data = currentData()?.clone()!!
        player.startTrack(currentTrack()?.data?.audioTrack!!,true)
        currentTrack()?.data?.seek(position)
        terminated = false
    }
}