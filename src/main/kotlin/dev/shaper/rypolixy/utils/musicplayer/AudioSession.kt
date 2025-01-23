package dev.shaper.rypolixy.utils.musicplayer

import dev.kord.common.annotation.KordVoice
import dev.kord.voice.VoiceConnection
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.shaper.rypolixy.utils.musicplayer.AudioPlayer.PlayerOptions

data class AudioSession @OptIn(KordVoice::class) constructor(
    val queue:          MutableList<AudioTrack>,
    val player:         AudioPlayer,
    val connection:     VoiceConnection,
    val options:        PlayerOptions
){
    var index:          Int = 0
    var paused:         Boolean = false
    fun currentTrack(): AudioTrack = queue[index]
}