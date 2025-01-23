package dev.shaper.rypolixy.utils.musicplayer.ytdlp

import com.jfposton.ytdlp.YtDlp
import com.jfposton.ytdlp.YtDlpRequest
import com.jfposton.ytdlp.mapper.VideoInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.shaper.rypolixy.config.Properties
import dev.shaper.rypolixy.utils.io.json.JsonManager

object YtDlpManager {

    init {
        YtDlp.setExecutablePath(Properties.getProperty("program.ytdlp"))
    }

    @JsonClass(generateAdapter = true)
    data class YtDlpBaseType(
        //base
        @Json(name = "id")          val id:             String?,
        @Json(name = "title")       val title:          String?,
        @Json(name = "url")         val url:            String?,
        @Json(name = "description") val description:    String?,
        @Json(name = "duration")    val duration:       Int?,
        @Json(name = "webpage_url") val webpageUrl:     String?,
        @Json(name = "thumbnail")   val thumbnail:      String?,
        @Json(name = "thumbnails")  val thumbnails:     List<Thumbnail>?,
        @Json(name = "uploader")    val uploader:       String?,
        @Json(name = "uploader_id") val uploaderId:     String?,
        @Json(name = "channel")     val channel:        String?,
        @Json(name = "channel_id")  val channelId:      String?,
        @Json(name = "channel_url") val channelUrl:     String?,
        @Json(name = "view_count")  val viewCount:      Int?,
        @Json(name = "like_count")  val likeCount:      Int?,
        @Json(name = "categories")  val categories:     List<String>?,
        @Json(name = "tags")        val tags:           List<String>?,
        //playlist
        @Json(name = "entries")                 val entries:            List<YtDlpBaseType>?, // 재생목록 항목
        @Json(name = "playlist_title")          val playlistTitle:      String?,
        @Json(name = "playlist_id")             val playlistId:         String?,
        @Json(name = "playlist_uploader")       val playlistUploader:   String?,
        @Json(name = "playlist_uploader_id")    val playlistUploaderId: String?,
        @Json(name = "playlist_index")          val playlistIndex:      Int?,
        @Json(name = "n_entries")               val totalEntries:       Int?,
        //stream
        @Json(name = "live_status") val liveStatus: String?,
        @Json(name = "is_live")     val isLive:     Boolean?,
        @Json(name = "start_time")  val startTime:  Long?,
        @Json(name = "end_time")    val endTime:    Long?
    )
    

    // 썸네일 정보를 위한 데이터 클래스
    data class Thumbnail(
        val url: String?,
        val height: Int?,
        val width: Int?
    )

    fun getPlaylistData(url:String): List<YtDlpBaseType> {
        val request = YtDlpRequest(url).apply {
            setOption("dump-json")
            setOption("quiet")
            setOption("flat-playlist")
            setOption("skip-download")
        }
        val result = YtDlp.execute(request).out.split("\n").filter { it.isNotBlank() }
        return result.map {
            JsonManager.encode<YtDlpBaseType>(it)
        }
    }

    fun getTrackInfo(url:String): VideoInfo {
        return YtDlp.getVideoInfo(url)
    }
}