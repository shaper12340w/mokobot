package dev.shaper.rypolixy.core.musicplayer

import dev.kord.common.annotation.KordVoice
import dev.kord.voice.VoiceConnection
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.kord.voice.AudioProvider
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils

data class MediaData @OptIn(KordVoice::class) constructor(
    val queue:          MutableList<MediaTrack>,
    val player:         AudioPlayer,
    val provider:       AudioProvider,
    val connection:     VoiceConnection,
    val connector:      MediaUtils.ConnectOptions,
    val options:        MediaUtils.QueueOptions
){

    fun current(): MediaTrack? = queue.getOrNull(options.index)

    fun currentTrack(): MediaTrack.Track?{
        return when(current()) {
            is MediaTrack.Track     -> queue[options.index] as MediaTrack.Track
            is MediaTrack.Playlist  -> {
                val trackData =  (queue[options.index] as MediaTrack.Playlist).tracks[options.subIndex]
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
        currentTrack()?.data?.seek(options.position)
        options.terminated = false
    }
}