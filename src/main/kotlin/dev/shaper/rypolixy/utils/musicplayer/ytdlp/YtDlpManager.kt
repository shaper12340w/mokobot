package dev.shaper.rypolixy.utils.musicplayer.ytdlp

import com.jfposton.ytdlp.YtDlp
import com.jfposton.ytdlp.YtDlpRequest
import com.jfposton.ytdlp.mapper.VideoInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.shaper.rypolixy.utils.io.json.JsonManager

object YtDlpManager {

    data class PlaylistInfo(
        @Json(name = "_type") val type: String,
        @Json(name = "id") val videoId: String,
        val url: String,
        val title: String,
        val duration: Int,
        @Json(name = "duration_string") val durationString: String,
        val channel: String,
        @Json(name = "channel_id") val channelId: String,
        @Json(name = "channel_url") val channelUrl: String,
        val uploader: String,
        @Json(name = "uploader_id") val uploaderId: String,
        @Json(name = "uploader_url") val uploaderUrl: String,
        val thumbnails: List<Thumbnail>,
        @Json(name = "view_count") val viewCount: Int,
        @Json(name = "webpage_url") val webpageUrl: String,
        val playlist: String,
        @Json(name = "playlist_id") val playlistId: String,
        @Json(name = "playlist_title") val playlistTitle: String,
        @Json(name = "playlist_index") val playlistIndex: Int,
        @Json(name = "n_entries") val totalEntries: Int
    )

    // 썸네일 정보를 위한 데이터 클래스
    data class Thumbnail(
        val url: String,
        val height: Int,
        val width: Int
    )

    fun getPlaylistData(url:String): List<PlaylistInfo> {
        val request = YtDlpRequest(url).apply {
            setOption("dump-json")
            setOption("quiet")
            setOption("flat-playlist")
            setOption("skip-download")
        }
        val result = YtDlp.execute(request).out.split("\n").filter { it.isNotBlank() }
        return result.map {
            JsonManager.encode<PlaylistInfo>(it)
        }
    }

    fun getTrackInfo(url:String): VideoInfo {
        return YtDlp.getVideoInfo(url)
    }
}