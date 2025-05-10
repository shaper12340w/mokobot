package dev.shaper.rypolixy.core.music.parser.soundcloud

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

class SoundcloudParseInfo {
    @JsonClass(generateAdapter = true)
    data class MediaFormat(
        @Json(name = "protocol")    val protocol: String,
        @Json(name = "mime_type")   val mimeType: String
    )

    @JsonClass(generateAdapter = true)
    data class Transcoding(
        @Json(name = "url")         val url: String,
        @Json(name = "preset")      val preset: String,
        @Json(name = "duration")    val duration: Int,
        @Json(name = "snipped")     val snipped: Boolean,
        @Json(name = "format")      val format: MediaFormat,
        @Json(name = "quality")     val quality: String
    )

    @JsonClass(generateAdapter = true)
    data class Media(
        @Json(name = "transcodings") val transCodings: List<Transcoding>
    )

    @JsonClass(generateAdapter = true)
    data class PublisherMetadata(
        @Json(name = "id")              val id: Int,
        @Json(name = "urn")             val urn: String,
        @Json(name = "artist")          val artist: String?,
        @Json(name = "album_title")     val albumTitle: String?,
        @Json(name = "contains_music")  val containsMusic: Boolean?,
        @Json(name = "publisher")       val publisher: String?,
        @Json(name = "isrc")            val isrc: String?,
        @Json(name = "release_title")   val releaseTitle: String?
    )

    @JsonClass(generateAdapter = true)
    data class Badges(
        @Json(name = "pro")             val pro: Boolean,
        @Json(name = "pro_unlimited")   val proUnlimited: Boolean,
        @Json(name = "verified")        val verified: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class User(
        @Json(name = "avatar_url")          val avatarUrl: String,
        @Json(name = "first_name")          val firstName: String,
        @Json(name = "followers_count")     val followersCount: Int,
        @Json(name = "full_name")           val fullName: String,
        @Json(name = "id")                  val id: Int,
        @Json(name = "kind")                val kind: String,
        @Json(name = "last_modified")       val lastModified: String,
        @Json(name = "last_name")           val lastName: String,
        @Json(name = "permalink")           val permalink: String,
        @Json(name = "permalink_url")       val permalinkUrl: String,
        @Json(name = "uri")                 val uri: String,
        @Json(name = "urn")                 val urn: String,
        @Json(name = "username")            val username: String,
        @Json(name = "verified")            val verified: Boolean,
        @Json(name = "city")                val city: String?,
        @Json(name = "country_code")        val countryCode: String?,
        @Json(name = "badges")              val badges: Badges,
        @Json(name = "station_urn")         val stationUrn: String,
        @Json(name = "station_permalink")   val stationPermalink: String
    )

    @JsonClass(generateAdapter = true)
    data class SoundCloudTrack(
        @Json(name = "artwork_url")         val artworkUrl: String?,
        @Json(name = "caption")             val caption: String?,
        @Json(name = "commentable")         val commentable: Boolean,
        @Json(name = "comment_count")       val commentCount: Int?,
        @Json(name = "created_at")          val createdAt: String,
        @Json(name = "description")         val description: String?,
        @Json(name = "downloadable")        val downloadable: Boolean,
        @Json(name = "download_count")      val downloadCount: Int,
        @Json(name = "duration")            val duration: Int,
        @Json(name = "full_duration")       val fullDuration: Int,
        @Json(name = "embeddable_by")       val embeddableBy: String,
        @Json(name = "genre")               val genre: String?,
        @Json(name = "has_downloads_left")  val hasDownloadsLeft: Boolean,
        @Json(name = "id")                  val id: Int,
        @Json(name = "kind")                val kind: String,
        @Json(name = "label_name")          val labelName: String?,
        @Json(name = "last_modified")       val lastModified: String,
        @Json(name = "license")             val license: String,
        @Json(name = "likes_count")         val likesCount: Int,
        @Json(name = "permalink")           val permalink: String,
        @Json(name = "permalink_url")       val permalinkUrl: String,
        @Json(name = "playback_count")      val playbackCount: Int,
        @Json(name = "public")              val public: Boolean,
        @Json(name = "publisher_metadata")  val publisherMetadata: PublisherMetadata?,
        @Json(name = "purchase_title")      val purchaseTitle: String?,
        @Json(name = "purchase_url")        val purchaseUrl: String?,
        @Json(name = "release_date")        val releaseDate: String?,
        @Json(name = "reposts_count")       val repostsCount: Int,
        @Json(name = "secret_token")        val secretToken: String?,
        @Json(name = "sharing")             val sharing: String,
        @Json(name = "state")               val state: String,
        @Json(name = "streamable")          val streamable: Boolean,
        @Json(name = "tag_list")            val tagList: String,
        @Json(name = "title")               val title: String,
        @Json(name = "track_format")        val trackFormat: String?,
        @Json(name = "uri")                 val uri: String,
        @Json(name = "urn")                 val urn: String,
        @Json(name = "user_id")             val userId: Int,
        @Json(name = "visuals")             val visuals: Visuals?,
        @Json(name = "waveform_url")        val waveformUrl: String,
        @Json(name = "display_date")        val displayDate: String,
        @Json(name = "media")               val media: Media,
        @Json(name = "station_urn")         val stationUrn: String,
        @Json(name = "station_permalink")   val stationPermalink: String,
        @Json(name = "track_authorization") val trackAuthorization: String,
        @Json(name = "monetization_model")  val monetizationModel: String,
        @Json(name = "policy")              val policy: String,
        @Json(name = "user")                val user: User
    )

    @JsonClass(generateAdapter = true)
    data class Visuals(
        @Json(name = "urn")             val urn: String,
        @Json(name = "enabled")         val enabled: Boolean,
        @Json(name = "tracking")        val tracking: Boolean?,
    )

    /**
     * API 응답의 루트 구조 중 관련 트랙 목록을 포함하는 객체 정의
     * (JSON의 "collection" 필드를 매핑)
     */
    @JsonClass(generateAdapter = true)
    data class RelatedResponse(
        @Json(name = "collection") val collection: List<SoundCloudTrack>?
    )
}