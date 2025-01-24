package dev.shaper.rypolixy.utils.musicplayer

import dev.shaper.rypolixy.utils.musicplayer.MediaType.MediaSource
import kotlin.time.Duration

sealed class MediaTrack {

    abstract val title:     String
    abstract val duration:  Duration
    abstract val url:       String?

    data class Track(
        override val title:     String,
        override val duration:  Duration,
        override val url:       String?,
        val id:         String,
        val author:     String,
        val thumbnail:  String?,
        val source:     MediaSource,
        val data:       MediaBehavior
    ) : MediaTrack() {

        fun hyperlink(): String {
            return "[$title ($duration)]($url)"
        }

    }

    data class Playlist(
        override val title:     String,
        override val duration:  Duration,
        override val url:       String?,
        val isSeek: Boolean,
        val tracks: List<Track>,
        val source: MediaSource
    ) : MediaTrack()

}