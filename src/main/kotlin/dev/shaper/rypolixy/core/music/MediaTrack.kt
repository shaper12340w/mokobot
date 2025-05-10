package dev.shaper.rypolixy.core.music

import dev.shaper.rypolixy.core.music.utils.MediaUtils
import dev.shaper.rypolixy.core.music.utils.MediaUtils.MediaPlatform
import kotlin.time.Duration

sealed class MediaTrack {

    abstract val title      : String
    abstract val duration   : Duration
    abstract val url        : String?
    abstract val thumbnail  : String?
    abstract val source     : MediaPlatform
    abstract val artist     : String

    data class Track(
        override val title      : String,
        override val duration   : Duration,
        override val url        : String?,
        override val source     : MediaPlatform,
        override val thumbnail  : String?,
        override val artist     : String,
        val id          : String,
        var data        : MediaBehavior
    ) : BaseTrack() {

        fun hyperlink(): String {
            return "[$title ($duration)]($url)"
        }

    }

    abstract class BaseTrack : MediaTrack()

    data class Playlist(
        override val title      : String,
        override val duration   : Duration,
        override val url        : String?,
        override val thumbnail  : String?,
        override val source     : MediaPlatform,
        override val artist     : String,
        val isSeek: Boolean,
        val tracks: MutableList<MediaTrack>,
    ) : MediaTrack()

    data class FlatTrack(
        override val title      : String,
        override val duration   : Duration,
        override val url        : String?,
        override val thumbnail  : String?,
        override val source     : MediaPlatform,
        override val artist     : String,
    ) :BaseTrack(){

        suspend fun toTrack(): Track? {
            return MediaUtils.implementTrack(this.url!!,source) as Track?
        }

    }

}