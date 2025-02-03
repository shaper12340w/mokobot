package dev.shaper.rypolixy.core.musicplayer.utils

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.core.musicplayer.MediaBehavior.Companion.toTrack
import dev.shaper.rypolixy.core.musicplayer.MediaTrack
import dev.shaper.rypolixy.core.musicplayer.lavaplayer.LavaPlayerManager
import dev.shaper.rypolixy.core.musicplayer.lavaplayer.LavaResult
import dev.shaper.rypolixy.core.musicplayer.ytdlp.YtDlpInfo
import dev.shaper.rypolixy.core.musicplayer.ytdlp.YtDlpManager
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MediaUtils {
    enum class MediaPlatform (
        val url     : List<String>,
        val icon    : String,
        val option  : String?,
    ) {
        YOUTUBE(
            listOf(
                "https?:\\/\\/(?:www\\.)?youtu(?:be\\.com\\/watch\\?v=|\\.be\\/)([\\w\\-\\_]*)(&(amp;)?\u200C\u200B[\\w\\?\u200C\u200B=]*)?",
                "https?:\\/\\/(?:www\\.)?youtube\\.com\\/clip\\/(?<id>[^\\/?#]+)",
                "https?:\\/\\/(?:www\\.)?youtube\\.com\\/?$",
                "https?:\\/\\/(?:www\\.)?youtube\\.com\\/(?:results|search)\\?.*?(?:search_query|q)=[^&]+",
                "https?:\\/\\/(?:www\\.)?youtube\\.com\\/shorts\\/(?<id>[\\w-]{11})",
                "https?:\\/\\/(?:www\\.)?youtube\\.com\\/watch\\?v=(?<id>[\\w-]{11})",
                "https?:\\/\\/music\\.youtube\\.com\\/search\\?.*?(?:search_query|q)=[^&]+",
                "https?:\\/\\/(?:\\w+\\.)?youtube\\.com\\/embed\\/live_stream\\/?\\?.*?channel=(?<id>[^&#]+)",
                "https?:\\/\\/youtu\\.be\\/([\\w-]{11})(?:[^#&]*[&?])list=((?:(?:PL|LL|EC|UU|FL|RD|UL|TL|PU|OLAK5uy_)[\\w-]{10,}|RDMM|WL|LL|LM))",
                "https?:\\/\\/www\\.youtube\\.com\\/playlist\\?list=([a-zA-Z0-9-_]+)"
            ),
            "https://www.youtube.com/favicon.ico",
            "ytsearch"
        ),
        SOUNDCLOUD(
            listOf(
                "https?:\\/\\/(soundcloud\\.com|snd\\.sc)\\/(.*)",
                "https?:\\/\\/(?:www\\.|m\\.)?soundcloud\\.com\\/[a-z0-9](?!.*?(-|_){2})[\\w-]{1,23}[a-z0-9](?:\\/.+)?",
                "https?:\\/\\/(soundcloud\\.com|snd\\.sc)\\/([A-Za-z0-9_-]+)\\/([A-Za-z0-9_-]+)\\/?"
            ),
            "https://soundcloud.com",
            "scsearch"
        ),
        SPOTIFY(
            listOf(
                "^(?:spotify:|https?:\\/\\/[a-z]+\\.spotify\\.com\\/(track\\/|user\\/(.*)\\/playlist\\/|playlist\\/))(.*)$"
            ),
            "https://spotify.com/",
            "spsearch"
        ),
        UNKNOWN(listOf(), "", null)
    }

    data class PlayerOptions(
        var leaveTime:      Long    = 0,
        var volume:         Double  = 100.0,
        var shuffle:        Boolean = false,
        var recommendation: Boolean = false,
    ){
        enum class RepeatType {
            DEFAULT, ONCE, ALL
        }
        var repeat: RepeatType = RepeatType.DEFAULT
    }

    data class ConnectOptions(
        val channel         : TopGuildMessageChannel,
        val voiceChannel    : VoiceChannel,
        val options         : PlayerOptions
    )

    data class QueueOptions(
        var index:          Int = 0,
        var subIndex:       Int = 0,
        var position:       Long = 0,
        var paused:         Boolean = false,
        var terminated:     Boolean = false,
    )

    sealed class SearchType {
        data object SUCCESS:SearchType()
        data object NORESULTS:SearchType()
        data class  ERROR(val exception: Exception):SearchType()
    }

    data class SearchResult(
        val status: SearchType,
        val data: MediaTrack?
    )



    companion object{

        /**
         * Convert MediaTrack(with no specific data) to implement it.
         * */
        suspend fun implementTrack(url: String, source: MediaPlatform = MediaPlatform.YOUTUBE): MediaTrack? {
            logger.debug { "Convert Track $url with $source" }
            return when(source){
                MediaPlatform.SOUNDCLOUD    -> {
                    val lavaResult = LavaPlayerManager.load(url)
                    if (lavaResult !is LavaResult.Success)
                        throw FriendlyException(
                            "LavaPlayer not found from $url",
                            FriendlyException.Severity.COMMON,
                            IllegalArgumentException()
                        )
                    lavaTrackBuilder(lavaResult.track,source)
                }
                else -> {
                    val dlpResult = YtDlpManager.getUrlData(url)
                    ytDlpTrackBuilder(dlpResult!!,source)
                }
            }
        }

        /**
         * Build MediaTrack from LavaPlayer AudioItem
         * */
        fun lavaTrackBuilder(track: AudioItem, source: MediaPlatform): MediaTrack? {
            fun trackfy(track: AudioTrack): MediaTrack.Track {
                return MediaTrack.Track(
                    track.info.title,
                    track.info.length.toDuration(DurationUnit.MILLISECONDS),
                    track.info.uri,
                    source,
                    track.info.artworkUrl,
                    track.info.identifier,
                    track.info.author,
                    track.toTrack()
                )
            }

            return when(track){
                is AudioTrack       -> trackfy(track)
                is AudioPlaylist    -> {
                    return MediaTrack.Playlist(
                        track.name,
                        track.tracks.fold(0L) { acc, audioTrack -> acc + audioTrack.duration }
                            .toDuration(DurationUnit.MINUTES),
                        track.selectedTrack?.info?.uri,
                        track.selectedTrack?.info?.artworkUrl,
                        source,
                        track.isSearchResult,
                        track.tracks.map { trackfy(it) }.toMutableList(),
                    )
                }
                else -> null
            }
        }

        suspend fun ytDlpTrackBuilder(info: YtDlpInfo, source: MediaPlatform): MediaTrack? {

            fun dlpTrackInfoToTrack(
                track   : YtDlpInfo.TrackInfo,
                lavaInfo: LavaResult.Success
            ) : MediaTrack.Track
            {
                return MediaTrack.Track(
                    source = source,
                    title = track.title,
                    duration = track.duration?.toDuration(DurationUnit.SECONDS) ?: Duration.ZERO,
                    url = track.pageUrl,
                    id = track.id,
                    author = track.channel ?: "Unknown",
                    thumbnail = track.thumbnail,
                    data = (lavaTrackBuilder(lavaInfo.track, source) as MediaTrack.Track).data
                )
            }

            fun dlpFlatTrackInfoToTrack(
                track : YtDlpInfo.FlatTrackInfo
            ) : MediaTrack.FlatTrack {
                return MediaTrack.FlatTrack(
                    title = track.title,
                    duration = track.duration?.toDuration(DurationUnit.SECONDS) ?: Duration.ZERO,
                    url = track.pageUrl,
                    thumbnail = track.thumbnails.getOrNull(0)?.url,
                    source = source
                )
            }

            fun dlpPlaylistInfoToPlaylist(
                url         : String,
                thumbnail   : String?,
                entries     : List<MediaTrack>,
                isSearch    : Boolean,
            ) : MediaTrack.Playlist {
                val checkDuration = entries.fold(0) { acc,trackInfo ->
                    when(trackInfo){
                        is MediaTrack.Track -> acc + trackInfo.duration.toInt(DurationUnit.SECONDS)
                        is MediaTrack.FlatTrack -> acc + trackInfo.duration.toInt(DurationUnit.SECONDS)
                        else -> acc + 0
                    }
                }
                return MediaTrack.Playlist(
                    source      = source,
                    title       = info.title,
                    duration    = checkDuration.toDuration(DurationUnit.SECONDS),
                    url         = url,
                    isSeek      = isSearch,
                    thumbnail   = thumbnail,
                    tracks      = entries.toMutableList()
                )
            }

            return when(info){
                is YtDlpInfo.TrackInfo          -> {
                    val lavaResult = LavaPlayerManager.load(info.streamUrl)
                    if(lavaResult !is LavaResult.Success)
                        throw RuntimeException("Cannot get source from lavaplayer URL : ${info.pageUrl}")
                    dlpTrackInfoToTrack(info,lavaResult)
                }
                is YtDlpInfo.PlaylistInfo       -> dlpPlaylistInfoToPlaylist(
                    info.pageUrl,
                    info.thumbnails.getOrNull(0)?.url,
                    info.entries.map {
                        val lavaResult = LavaPlayerManager.load(it.pageUrl)
                        if(lavaResult !is LavaResult.Success)
                            throw RuntimeException("Cannot get source from lavaplayer URL : ${it.pageUrl}")
                        dlpTrackInfoToTrack(it,lavaResult)
                    },
                    false
                )
                is YtDlpInfo.FlatPlaylistInfo   -> dlpPlaylistInfoToPlaylist(
                    info.pageUrl,
                    info.thumbnails?.getOrNull(0)?.url,
                    info.entries.map { dlpFlatTrackInfoToTrack(it) },
                    false
                )
                is YtDlpInfo.SearchTrackInfo    -> dlpPlaylistInfoToPlaylist(
                    info.pageUrl,
                    null,
                    info.entries.map { dlpFlatTrackInfoToTrack(it) },
                    true
                )

                else -> null
            }

        }

    }


}