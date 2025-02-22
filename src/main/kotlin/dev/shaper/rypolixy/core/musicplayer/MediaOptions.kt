package dev.shaper.rypolixy.core.musicplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.kord.common.annotation.KordVoice
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.voice.AudioProvider
import dev.kord.voice.VoiceConnection
import java.util.*

sealed class MediaOptions {

    sealed class SearchType {
        data object SUCCESS     :SearchType()
        data object NORESULTS   :SearchType()
        data class  ERROR(val exception: Exception):SearchType()
    }

    data class SearchResult(
        val status: SearchType,
        val data: MediaTrack?
    )

    enum class RepeatType {
        DEFAULT, ONCE, ALL
    }

    enum class PlayerStatus{
        IDLE, PAUSED, PLAYING, TERMINATED, ERROR
    }

    data class PlayerData @OptIn(KordVoice::class) constructor(
        val audio           : AudioPlayer,
        val provider        : AudioProvider,
        val connection      : VoiceConnection,
        var status          : PlayerStatus  = PlayerStatus.IDLE,
    )

    data class PlayerOptions(
        var leaveTime       : Long    = 0,
        var volume          : Double  = 100.0,
        var shuffle         : Boolean = false,
        var recommendation  : Boolean = false,
        var repeat          : RepeatType = RepeatType.DEFAULT
    )

    data class ChannelOptions(
        val messageChannel  : TopGuildMessageChannel,
        val voiceChannel    : VoiceChannel
    )

    data class QueueOptions(
        val tracks              : MutableList<MediaTrack>,
        var index               : Int           = 0,
        var subIndex            : Int           = 0,

        internal var position   : Long          = 0,
        internal var timer      : Timer         = Timer(),

    )
}