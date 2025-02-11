package dev.shaper.rypolixy.core.musicplayer

import dev.kord.common.annotation.KordVoice
import dev.kord.voice.VoiceConnection
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.kord.voice.AudioProvider
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils.Companion.implementTrack

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

    fun currentBaseTrack(): MediaTrack.BaseTrack? {
        return when(current()){
            is MediaTrack.Playlist,
            is MediaTrack.Track     -> currentTrack()
            is MediaTrack.FlatTrack -> queue[options.index] as MediaTrack.BaseTrack?
            else -> null
        }
    }

    fun update() {
        currentTrack()?.data = currentTrack()?.data?.clone() ?: return
        player.startTrack(currentTrack()?.data?.audioTrack!!,true)
        currentTrack()?.data?.seek(options.position)
        options.terminated = false
    }

    suspend fun reload(){
        val track = currentTrack() ?: return
        val searchedData = implementTrack(track.url!!,track.source) ?: return
        when(val current = queue[options.index]){
            is MediaTrack.FlatTrack,
            is MediaTrack.Track     -> queue[options.index] = searchedData
            is MediaTrack.Playlist  -> current.tracks[options.subIndex] = searchedData
            else                    -> Unit
        }
        update()
    }
}