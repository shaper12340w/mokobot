package dev.shaper.rypolixy.utils.musicplayer

import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.voice.VoiceConnection
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.kord.core.behavior.channel.ChannelBehavior

data class AudioSession @OptIn(KordVoice::class) constructor(
    val queue:          MutableList<AudioTrack>,
    val player:         AudioPlayer,
    val connection:     VoiceConnection,
    val voiceChannel:   BaseVoiceChannelBehavior,
    val textChannel:    ChannelBehavior
){
    enum class RepeatType {
        DEFAULT, ONCE, ALL
    }
    var index:          Int = 0
    var volume:         Double = 100.0
    var repeat: RepeatType = RepeatType.DEFAULT
    var shuffle:        Boolean = false
    var paused:         Boolean = false
    fun currentTrack(): AudioTrack = queue[index]
}