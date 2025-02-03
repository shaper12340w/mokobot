package dev.shaper.rypolixy.core.musicplayer.ytdlp

import com.jfposton.ytdlp.YtDlp
import com.jfposton.ytdlp.YtDlpException
import com.jfposton.ytdlp.YtDlpRequest
import com.jfposton.ytdlp.mapper.VideoInfo
import dev.shaper.rypolixy.config.Configs
import dev.shaper.rypolixy.config.Properties
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.io.json.JsonManager
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

object YtDlpManager {

    init {
        YtDlp.setExecutablePath(Configs.PROGRAMS.ytdlp)
    }

    enum class DataType(val value: String?){
        TRACK("video"),
        PLAYLIST("playlist"),
        ENTRY(null),
    }

    suspend fun getUrlData(url:String, isFlat:Boolean = true): YtDlpInfo? {
        logger.debug { "Get yt-dlp data from url : $url" }
        try {
            return withTimeout(60000L){
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
                    return@withTimeout JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.FlatPlaylistInfo::class).decode<YtDlpInfo.FlatPlaylistInfo>(result)
                when(checkType.type) {
                    DataType.TRACK.value    -> return@withTimeout JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.TrackInfo::class).decode<YtDlpInfo.TrackInfo>(result)
                    DataType.PLAYLIST.value -> return@withTimeout JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.PlaylistInfo::class).decode<YtDlpInfo.PlaylistInfo>(result)
                }
                return@withTimeout null
            }
        }
        catch (e: TimeoutCancellationException) {
            logger.error { "Timeout while getting yt-dlp data from url : $url" }
            return null
        }
        catch (e: YtDlpException) {
            logger.error { "Cannot Get yt-dlp data from url : $url" }
            logger.error { e.message }
            return null
        }
    }

    suspend fun getSearchData(arg:String, platform: MediaUtils.MediaPlatform, count:Int = 10): YtDlpInfo.SearchTrackInfo? {
        try {
            return withTimeout(60000L) {
                val questArg    = "${platform.option}$count:\"$arg\""
                val request     = YtDlpRequest(questArg).apply {
                    setOption("quiet")
                    setOption("dump-single-json")
                    setOption("skip-download")
                    setOption("flat-playlist")
                }
                logger.debug { questArg }
                val result      = YtDlp.execute(request).out
                val serialized  = JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.SearchTrackInfo::class).decode<YtDlpInfo.SearchTrackInfo>(result)
                logger.debug { serialized }
                return@withTimeout serialized
            }
        }
        catch (e: TimeoutCancellationException) {
            logger.error { "Timeout while getting yt-dlp data from arg : $arg" }
            return null
        }
        catch (e: YtDlpException) {
            logger.error { "Cannot Get yt-dlp data from arg : $arg" }
            logger.error { e.message }
            return null
        }
    }

    @Deprecated("Deprecated. Use getData", ReplaceWith("getData(url, isFlat)"))
    fun getTrackInfo(url:String): VideoInfo {
        return YtDlp.getVideoInfo(url)
    }
}