package dev.shaper.rypolixy.utils.musicplayer.ytdlp

import com.jfposton.ytdlp.YtDlp
import com.jfposton.ytdlp.YtDlpRequest
import com.jfposton.ytdlp.mapper.VideoInfo
import dev.shaper.rypolixy.config.Properties
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.io.json.JsonManager

object YtDlpManager {

    init {
        YtDlp.setExecutablePath(Properties.getProperty("program.ytdlp"))
    }

    enum class DataType(val value: String?){
        TRACK("video"),
        PLAYLIST("playlist"),
        ENTRY(null),
    }

    fun getData(url:String, isFlat:Boolean = true): YtDlpInfo? {
        val request = YtDlpRequest(url).apply {
            setOption("quiet")
            setOption("dump-single-json")
            setOption("skip-download")
            if(isFlat)
                setOption("flat-playlist")
        }
        val result      = YtDlp.execute(request).out
        logger.debug { "Data: $result" }
        val checkType   = JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.BaseInfo::class).decode<YtDlpInfo.BaseInfo>(result)
        logger.debug { "Check type: $checkType" }
        if(isFlat && (checkType.type == DataType.PLAYLIST.value))
            return JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.FlatPlaylistInfo::class).decode<YtDlpInfo.FlatPlaylistInfo>(result)
        when(checkType.type) {
            DataType.TRACK.value    -> return JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.TrackInfo::class).decode<YtDlpInfo.TrackInfo>(result)
            DataType.PLAYLIST.value -> return JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.PlaylistInfo::class).decode<YtDlpInfo.PlaylistInfo>(result)
        }
        return null
    }

    @Deprecated("Deprecated. Use getData", ReplaceWith("getData(url, isFlat)"))
    fun getTrackInfo(url:String): VideoInfo {
        return YtDlp.getVideoInfo(url)
    }
}