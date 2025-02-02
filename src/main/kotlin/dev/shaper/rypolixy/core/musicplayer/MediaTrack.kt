package dev.shaper.rypolixy.core.musicplayer

import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils.MediaPlatform
import kotlin.time.Duration

sealed class MediaTrack {

    abstract val title      : String
    abstract val duration   : Duration
    abstract val url        : String?
    abstract val thumbnail  : String?
    abstract val source     : MediaPlatform

    data class Track(
        override val title      : String,
        override val duration   : Duration,
        override val url        : String?,
        override val source     : MediaPlatform,
        override val thumbnail  : String?,
        val id          : String,
        val author      : String,
        var data        : MediaBehavior
    ) : MediaTrack() {

        fun hyperlink(): String {
            return "[$title ($duration)]($url)"
        }

    }

    data class Playlist(
        override val title      : String,
        override val duration   : Duration,
        override val url        : String?,
        override val thumbnail  : String?,
        override val source     : MediaPlatform,
        val isSeek: Boolean,
        val tracks: MutableList<MediaTrack>,
    ) : MediaTrack()

    data class FlatTrack(
        override val title      : String,
        override val duration   : Duration,
        override val url        : String?,
        override val thumbnail  : String?,
        override val source     : MediaPlatform,
    ) :MediaTrack(){

        suspend fun toTrack(): Track? {
            return MediaUtils.implementTrack(this,source) as Track?
        }

    }

}