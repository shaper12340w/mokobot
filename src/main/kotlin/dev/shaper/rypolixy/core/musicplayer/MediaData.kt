package dev.shaper.rypolixy.core.musicplayer

import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils.Companion.implementTrack

data class MediaData(
    val queue       : MediaOptions.QueueOptions,
    val player      : MediaOptions.PlayerData,
    val options     : MediaOptions.PlayerOptions,
    val channels    : MediaOptions.ChannelOptions
){

    fun current(): MediaTrack? = queue.tracks.getOrNull(queue.index)

    fun currentTrackOrNull(): MediaTrack.Track?{
        return when(current()) {
            is MediaTrack.Track     -> queue.tracks[queue.index] as MediaTrack.Track
            is MediaTrack.Playlist  -> {
                val trackData =  (queue.tracks[queue.index] as MediaTrack.Playlist).tracks[queue.subIndex]
                if(trackData is MediaTrack.Track) trackData
                else null
            }
            else -> null
        }
    }

    fun currentTrack() : MediaTrack.Track
            = currentTrackOrNull() ?: throw Exception("Current Track is not set")


    fun update() {
        val track = currentTrack()
        track.data = track.data.clone()
        player.audio.startTrack(track.data.audioTrack,true)
        track.data.seek(queue.position)
        player.status = MediaOptions.PlayerStatus.PLAYING
    }

    suspend fun reload(){
        val track = currentTrack()
        val current = queue.tracks[queue.index]
        val searchedData = implementTrack(track.url!!,track.source) ?: return
        when(current){
            is MediaTrack.FlatTrack,
            is MediaTrack.Track     -> queue.tracks[queue.index]        = searchedData
            is MediaTrack.Playlist  -> current.tracks[queue.subIndex]   = searchedData
            else                    -> Unit
        }
        update()
    }
}