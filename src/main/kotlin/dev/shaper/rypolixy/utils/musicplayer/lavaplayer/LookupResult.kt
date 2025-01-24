package dev.shaper.rypolixy.utils.musicplayer.lavaplayer

import com.sedmelluq.discord.lavaplayer.track.AudioItem

sealed class LookupResult {

    data object Error : LookupResult()
    data object NoResults : LookupResult()

    data class Success(
        val track: AudioItem
    ) : LookupResult()

}