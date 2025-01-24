package dev.shaper.rypolixy.utils.musicplayer

import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.shaper.rypolixy.utils.musicplayer.MediaBehavior.Companion.toTrack
import dev.shaper.rypolixy.utils.musicplayer.lavaplayer.LookupResult
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MediaType {
    enum class MediaSource(
        val url     : List<String>,
        val icon    : String,
        val option  : String?,
    ) {
        YOUTUBE(
            listOf(
                "https?://(?:www\\.)?youtube\\.com/clip/(?P<id>[^/?#]+)",
                "https?://(?:www\\.)?youtube\\.com/?(?:[?#]|$)|:ytrec(?:ommended)?",
                "https?://(?:www\\.)?youtube\\.com/(?:results|search)\\?([^#]+&)?(?:search_query|q)=(?:[^&]+)(?:[&#]|$)",
                "https?://(?:www\\.)?youtube\\.com/source/(?P<id>[\\w-]{11})/shorts",
                "https?://(?:www\\.)?youtube\\.com/watch\\?v=(?P<id>[0-9A-Za-z_-]{1,10})$",
                "https?://music\\.youtube\\.com/search\\?([^#]+&)?(?:search_query|q)=(?:[^&]+)(?:[&#]|$)",
                "https?://(?:\\w+\\.)?youtube\\.com/embed/live_stream/?\\?(?:[^#]+&)?channel=(?P<id>[^&#]+)",
                "https?://youtu\\.be/(?P<id>[0-9A-Za-z_-]{11})/*?.*?\\blist=(?P<playlist_id>(?:(?:PL|LL|EC|UU|FL|RD|UL|TL|PU|OLAK5uy_)[0-9A-Za-z-_]{10,}|RDMM|WL|LL|LM))"
                ),
            "https://www.youtube.com/favicon.ico",
            "ytsearch"
        ),
        SOUNDCLOUD(
            listOf(
                "/^https?:\\/\\/(soundcloud\\.com|snd\\.sc)\\/(.*)\$/",
                "^(?:https?:\\/\\/)((?:www\\.)|(?:m\\.))?soundcloud\\.com\\/[a-z0-9](?!.*?(-|_){2})[\\w-]{1,23}[a-z0-9](?:\\/.+)?\$",
                "/^https?:\\/\\/(soundcloud\\.com|snd\\.sc)\\/([A-Za-z0-9_-]+)\\/([A-Za-z0-9_-]+)\\/?\$/",
            ),
            "https://soundcloud.com",
            "scsearch"
        ),
        SPOTIFY(
            listOf(
                "/^(?:spotify:|https:\\/\\/[a-z]+\\.spotify\\.com\\/(track\\/|user\\/(.*)\\/playlist\\/|playlist\\/))(.*)\$/"
            ),
            "https://spotify.com/",
            "spsearch",
        ),
        UNKNOWN(listOf(),"",null)

    }

    data class PlayerOptions(
        var leaveTime:      Long = 0,
        var volume:         Double = 100.0,
        var shuffle:        Boolean = false
    ){
        enum class RepeatType {
            DEFAULT, ONCE, ALL
        }
        var repeat: RepeatType = RepeatType.DEFAULT
    }

    data class ConnectOptions(
        val channel         : ChannelBehavior,
        val voiceChannel    : BaseVoiceChannelBehavior,
        val playerOptions   : PlayerOptions
    )


    data class SearchResult(
        val result : LookupResult,
        val data   : MediaTrack?,
    )


    companion object{

        fun trackBuilder(track: AudioItem, source: MediaSource):MediaTrack? {
            fun trackfy(track: AudioTrack):MediaTrack.Track{
                return MediaTrack.Track(
                    track.info.title,
                    track.info.length.toDuration(DurationUnit.MILLISECONDS),
                    track.info.uri,
                    track.info.identifier,
                    track.info.author,
                    track.info.artworkUrl,
                    source,
                    track.toTrack()
                )
            }

            return when(track){
                is AudioTrack       -> trackfy(track)
                is AudioPlaylist    -> {
                    return MediaTrack.Playlist(
                        track.name,
                        track.tracks.fold(0L) { acc, audioTrack -> acc + audioTrack.duration }.toDuration(DurationUnit.MINUTES),
                        track.selectedTrack?.info?.uri,
                        track.isSearchResult,
                        track.tracks.map { trackfy(it) },
                        source
                    )
                }
                else -> null
            }
        }

    }


}