package dev.shaper.rypolixy.core.musicplayer.parser.youtube

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

class YoutubeParseInfo {
    @JsonClass(generateAdapter = true)
    data class CompactVideoInfo(
        @Json(name = "compactVideoRenderer") val videoRenderer: RelatedVideo

    )
    @JsonClass(generateAdapter = true)
    data class RelatedVideo(
        @Json(name = "videoId")             val videoId: String,
        @Json(name = "title")               val title: VideoTitle,
        @Json(name = "lengthText")          val lengthText: SimpleText,
        @Json(name = "viewCountText")       val viewCountText: SimpleText,
        @Json(name = "publishedTimeText")   val publishedTimeText: SimpleText,
        @Json(name = "thumbnail")           val thumbnail: Thumbnail,
        @Json(name = "shortBylineText")     val channelInfo: ShortBylineText
    )

    @JsonClass(generateAdapter = true)
    data class VideoTitle(
        @Json(name = "simpleText") val text: String
    )

    @JsonClass(generateAdapter = true)
    data class SimpleText(
        @Json(name = "simpleText") val text: String
    )

    @JsonClass(generateAdapter = true)
    data class Thumbnail(
        @Json(name = "thumbnails") val thumbnails: List<ThumbnailItem>
    )

    @JsonClass(generateAdapter = true)
    data class ThumbnailItem(
        @Json(name = "url")     val url: String,
        @Json(name = "width")   val width: Int,
        @Json(name = "height")  val height: Int
    )

    @JsonClass(generateAdapter = true)
    data class ShortBylineText(
        @Json(name = "runs")    val runs: List<ChannelRun>
    )

    @JsonClass(generateAdapter = true)
    data class ChannelRun(
        @Json(name = "text")    val channelName: String
    )
}