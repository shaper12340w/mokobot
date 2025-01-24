package dev.shaper.rypolixy.utils.musicplayer

import dev.kord.common.annotation.KordVoice
import dev.kord.voice.VoiceConnection
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer

data class MediaData @OptIn(KordVoice::class) constructor(
    val queue:          MutableList<MediaTrack.Track>,
    val player:         AudioPlayer,
    val connection:     VoiceConnection,
    val options:        MediaType.PlayerOptions,
){
    var index:          Int = 0
    var paused:         Boolean = false
    fun currentTrack(): MediaBehavior = queue[index].data
}