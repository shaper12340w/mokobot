package dev.shaper.rypolixy.utils.musicplayer.ytdlp

import com.jfposton.ytdlp.YtDlp
import com.jfposton.ytdlp.YtDlpRequest
import com.jfposton.ytdlp.mapper.VideoInfo
import dev.shaper.rypolixy.config.Properties
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.io.json.JsonManager
import dev.shaper.rypolixy.utils.musicplayer.MediaUtils

object YtDlpManager {

    init {
        YtDlp.setExecutablePath(Properties.getProperty("program.ytdlp"))
    }

    enum class DataType(val value: String?){
        TRACK("video"),
        PLAYLIST("playlist"),
        ENTRY(null),
    }

    fun getUrlData(url:String, isFlat:Boolean = true): YtDlpInfo? {
        logger.debug { "Get yt-dlp data from url : $url" }
        val request     = YtDlpRequest(url).apply {
            setOption("quiet")
            setOption("dump-single-json")
            setOption("skip-download")
            if(isFlat)
                setOption("flat-playlist")
        }
        val result      = YtDlp.execute(request).out
        val checkType   = JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.BaseInfo::class).decode<YtDlpInfo.BaseInfo>(result)
        if(isFlat && (checkType.type == DataType.PLAYLIST.value))
            return JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.FlatPlaylistInfo::class).decode<YtDlpInfo.FlatPlaylistInfo>(result)
        when(checkType.type) {
            DataType.TRACK.value    -> return JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.TrackInfo::class).decode<YtDlpInfo.TrackInfo>(result)
            DataType.PLAYLIST.value -> return JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.PlaylistInfo::class).decode<YtDlpInfo.PlaylistInfo>(result)
        }
        return null
    }

    fun getSearchData(arg:String, platform:MediaUtils.MediaPlatform, count:Int = 10): YtDlpInfo.SearchTrackInfo {
        val request     = YtDlpRequest("${platform.option}$count:\"$arg\"").apply {
            setOption("quiet")
            setOption("dump-single-json")
            setOption("skip-download")
            setOption("flat-playlist")
        }
        val result      = YtDlp.execute(request).out
        return JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.SearchTrackInfo::class).decode<YtDlpInfo.SearchTrackInfo>(result)
    }

    @Deprecated("Deprecated. Use getData", ReplaceWith("getData(url, isFlat)"))
    fun getTrackInfo(url:String): VideoInfo {
        return YtDlp.getVideoInfo(url)
    }
}