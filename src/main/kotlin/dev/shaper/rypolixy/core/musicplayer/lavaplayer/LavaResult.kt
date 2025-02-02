package dev.shaper.rypolixy.core.musicplayer.lavaplayer

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem

sealed class LavaResult {

    data object NoResults   : LavaResult()

    data class Error(
        val error: FriendlyException
    )  : LavaResult()

    data class Success(
        val track: AudioItem
    ) : LavaResult()

}