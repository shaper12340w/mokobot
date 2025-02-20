package dev.shaper.rypolixy.core.musicplayer.ytdlp

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true, generator = "sealed")
sealed class YtDlpInfo{
    //base
    abstract val type             : String?
    abstract val id               : String
    abstract val title            : String
    abstract val description      : String?
    abstract val viewCount        : Long?
    abstract val channel          : String?
    abstract val channelId        : String?
    abstract val channelUrl       : String?
    abstract val uploader         : String?
    abstract val uploaderId       : String?
    abstract val uploaderUrl      : String?
    abstract val availability     : String? //nullable

    //It has four types (Video=>Track,Playlist,Entry,FlatEntry)

    @JsonClass(generateAdapter = true)
    data class Thumbnail(
        val url         : String,
        val width       : Int?,
        val height      : Int?,
        val id          : String?,
        val preference  : Int?,
        val resolution  : String?,
    )

    @JsonClass(generateAdapter = true)
    data class Chapter(
        @Json(name = "start_time")  val startTime   : Float,
        @Json(name = "end_time")    val endTime     : Float,
        @Json(name = "title")       val title       : String,
    )

    @JsonClass(generateAdapter = true)
    data class TrackInfo(
        //@Deprecated("formats have too many data and have to process") //videoFormat.. etc
        @Json(name = "thumbnail")                val thumbnail       : String?,  //nullable
        @Json(name = "duration")                 val duration        : Double?,
        @Json(name = "webpage_url")              val pageUrl         : String,
        @Json(name = "categories")               val categories      : List<String>?,
        @Json(name = "tags")                     val tags            : List<String>?,
        @Json(name = "live_status")              val liveStatus      : String?,   //not_live
        @Json(name = "upload_date")              val uploadDate      : String?,   //YYYYMMDD
        @Json(name = "duration_string")          val durationString  : String?,   //hh:mm:ss
        @Json(name = "like_count")               val likeCount       : Int?,
        @Json(name = "comment_count")            val commentCount    : Int?,
        @Json(name = "channel_follower_count")   val followerCount   : Int?,
        @Json(name = "chapters")                 val chapters        : List<Chapter>?,
        @Json(name = "url")                      val streamUrl       : String,
        @Json(name = "is_live")                  val isLive          : Boolean?,
        @Json(name = "was_live")                 val wasLive         : Boolean?,
        @Json(name = "thumbnails")               val thumbnails     : List<Thumbnail>?,
        @Json(name = "_type")           override val type           : String?,
        @Json(name = "id")              override val id             : String,
        @Json(name = "title")           override val title          : String,
        @Json(name = "description")     override val description    : String?,
        @Json(name = "view_count")      override val viewCount      : Long?,
        @Json(name = "channel")         override val channel        : String?,
        @Json(name = "channel_id")      override val channelId      : String?,
        @Json(name = "channel_url")     override val channelUrl     : String?,
        @Json(name = "uploader")        override val uploader       : String?,
        @Json(name = "uploader_id")     override val uploaderId     : String?,
        @Json(name = "uploader_url")    override val uploaderUrl    : String?,
        @Json(name = "availability")    override val availability   : String?

    ):YtDlpInfo()

    //playlist
    @JsonClass(generateAdapter = true)
    data class PlaylistInfo(
        @Json(name = "channel_follower_count")   val followerCount  : Int?,
        @Json(name = "modified_date")            val modifiedDate   : String,   //YYYYMMDD
        @Json(name = "webpage_url")              val pageUrl        : String,
        @Json(name = "tags")                     val tags           : List<String>,
        @Json(name = "entries")                  val entries        : List<TrackInfo>,
        @Json(name = "playlist_count")           val playlistCount  : Int,
        @Json(name = "thumbnails")               val thumbnails     : List<Thumbnail>,
        @Json(name = "_type")           override val type           : String?,
        @Json(name = "id")              override val id             : String,
        @Json(name = "title")           override val title          : String,
        @Json(name = "description")     override val description    : String,
        @Json(name = "view_count")      override val viewCount      : Long,
        @Json(name = "channel")         override val channel        : String,
        @Json(name = "channel_id")      override val channelId      : String,
        @Json(name = "channel_url")     override val channelUrl     : String,
        @Json(name = "uploader")        override val uploader       : String,
        @Json(name = "uploader_id")     override val uploaderId     : String,
        @Json(name = "uploader_url")    override val uploaderUrl    : String,
        @Json(name = "availability")    override val availability   : String?
    ): YtDlpInfo()

    @JsonClass(generateAdapter = true)
    data class FlatPlaylistInfo(
        @Json(name = "webpage_url")              val pageUrl        : String,
        @Json(name = "entries")                  val entries        : List<FlatTrackInfo>,
        @Json(name = "playlist_count")           val playlistCount  : Int,
        @Json(name = "tags")                     val tags           : List<String>?,
        @Json(name = "thumbnails")               val thumbnails     : List<Thumbnail>?,
        @Json(name = "_type")           override val type           : String?,
        @Json(name = "id")              override val id             : String,
        @Json(name = "title")           override val title          : String,
        @Json(name = "description")     override val description    : String?,
        @Json(name = "view_count")      override val viewCount      : Long?,
        @Json(name = "channel")         override val channel        : String?,
        @Json(name = "channel_id")      override val channelId      : String?,
        @Json(name = "channel_url")     override val channelUrl     : String?,
        @Json(name = "uploader")        override val uploader       : String?,
        @Json(name = "uploader_id")     override val uploaderId     : String?,
        @Json(name = "uploader_url")    override val uploaderUrl    : String?,
        @Json(name = "availability")    override val availability   : String?

    ): YtDlpInfo()

    @JsonClass(generateAdapter = true)
    data class FlatTrackInfo(
        @Json(name = "duration")                 val duration       : Double?,
        @Json(name = "live_status")              val liveStatus     : String?,   //not_live
        @Json(name = "url")                      val pageUrl        : String,
        @Json(name = "ie_key")                   val platform       : String,
        @Json(name = "thumbnails")               val thumbnails     : List<Thumbnail>,
        @Json(name = "_type")           override val type           : String?,
        @Json(name = "id")              override val id             : String,
        @Json(name = "title")           override val title          : String,
        @Json(name = "description")     override val description    : String?,
        @Json(name = "view_count")      override val viewCount      : Long?,
        @Json(name = "channel")         override val channel        : String?,
        @Json(name = "channel_id")      override val channelId      : String?,
        @Json(name = "channel_url")     override val channelUrl     : String?,
        @Json(name = "uploader")        override val uploader       : String?,
        @Json(name = "uploader_id")     override val uploaderId     : String?,
        @Json(name = "uploader_url")    override val uploaderUrl    : String?,
        @Json(name = "availability")    override val availability   : String?
    ):YtDlpInfo()

    @JsonClass(generateAdapter = true)
    data class SearchTrackInfo(
        @Json(name = "webpage_url")     val pageUrl       : String,
        @Json(name = "entries")         val entries       : List<FlatTrackInfo>,
        @Json(name = "playlist_count")  val playlistCount : Int,
        @Json(name = "_type")           override val type           : String,
        @Json(name = "id")              override val id             : String,
        @Json(name = "title")           override val title          : String,
        @Json(name = "description")     override val description    : String?,
        @Json(name = "view_count")      override val viewCount      : Long?   ,
        @Json(name = "channel")         override val channel        : String?,
        @Json(name = "channel_id")      override val channelId      : String?,
        @Json(name = "channel_url")     override val channelUrl     : String?,
        @Json(name = "uploader")        override val uploader       : String?,
        @Json(name = "uploader_id")     override val uploaderId     : String?,
        @Json(name = "uploader_url")    override val uploaderUrl    : String?,
        @Json(name = "availability")    override val availability   : String?
    ):YtDlpInfo()

    @JsonClass(generateAdapter = true)
    data class BaseInfo(
        @Json(name = "_type")           override val type           : String?,
        @Json(name = "id")              override val id             : String,
        @Json(name = "title")           override val title          : String,
        @Json(name = "description")     override val description    : String?,
        @Json(name = "view_count")      override val viewCount      : Long?,
        @Json(name = "channel")         override val channel        : String?,
        @Json(name = "channel_id")      override val channelId      : String?,
        @Json(name = "channel_url")     override val channelUrl     : String?,
        @Json(name = "uploader")        override val uploader       : String?,
        @Json(name = "uploader_id")     override val uploaderId     : String?,
        @Json(name = "uploader_url")    override val uploaderUrl    : String?,
        @Json(name = "availability")    override val availability   : String?
    ):YtDlpInfo()

}