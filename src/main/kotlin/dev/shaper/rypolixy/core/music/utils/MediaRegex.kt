package dev.shaper.rypolixy.core.music.utils

import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

object MediaRegex {

    data class YoutubeRegexConfig(
        val youtubeBaseUrl: String,
        val youtubeVideoUrl: Regex,
        val youtubePlaylistUrl: Regex,
        val youtubeMusicUrl: Regex
    )

    data class SoundcloudRegexConfig(
        val soundcloudBaseUrl: String,
        val soundcloudApiVersion: String,
        val soundcloudUrlRegex: Regex,
        val regexTrack: Regex,
        val regexSet: Regex,
        val regexArtist: Regex,
        val streamFetchHeaders: Map<String, String>,
        val userUrnPattern: Regex,
        val streamErrors: Map<String, String>
    )

    data class RegexConfig(
        val youtube: YoutubeRegexConfig,
        val spotify: Regex,
        val deezer: Regex,
        val soundcloud: SoundcloudRegexConfig,
        val apple: Regex,
        val nico: Regex
    )

    val REGEX = RegexConfig(
        youtube = YoutubeRegexConfig(
            youtubeBaseUrl = "https://www.youtube.com",
            youtubeVideoUrl = Regex(
                "(?:https?://)?(?:www.)?youtu(?:.be/|be.com/\\S*(?:watch|embed)(?:(?=/[A-Za-z0-9_-]{11,}(?!\\S))/|(?:\\S*v=|v/)))([-A-Za-z0-9_]{11,})"
            ),
            youtubePlaylistUrl = Regex(
                "(?:youtube.com/watch\\?.*?list=|youtube.com/playlist\\?list=)([^&\\s]+)",
                RegexOption.IGNORE_CASE
            ),
            youtubeMusicUrl = Regex(
                "(?:https?://)?music.youtube.com/(?:watch\\?v=|v/)([^&\\s]+)",
                RegexOption.IGNORE_CASE
            )
        ),
        spotify = Regex(
            "^(?:spotify:|https://[a-z]+.spotify.com/(track/|user/(.*)/playlist/|playlist/))(.*)$"
        ),
        deezer = Regex(
            "^https?://(?:www.)?deezer.com/[a-z]+/(track|album|playlist)/(\\d+)$"
        ),
        soundcloud = SoundcloudRegexConfig(
            soundcloudBaseUrl = "https://soundcloud.com",
            soundcloudApiVersion = "/version.txt",
            soundcloudUrlRegex = Regex(
                "^https?://(soundcloud.com|snd.sc)/(.*)$"
            ),
            regexTrack = Regex(
                "^https?://(soundcloud.com|snd.sc)/([A-Za-z0-9_-]+)/([A-Za-z0-9_-]+)/?$"
            ),
            regexSet = Regex(
                "^https?://(soundcloud.com|snd.sc)/([A-Za-z0-9_-]+)/sets/([A-Za-z0-9_-]+)/?$"
            ),
            regexArtist = Regex(
                "^https?://(soundcloud.com|snd.sc)/([A-Za-z0-9_-]+)/?$"
            ),
            streamFetchHeaders = mapOf(
                "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.129 Safari/537.36",
                "Accept" to "*/*",
                "Accept-Encoding" to "gzip, deflate, br"
            ),
            userUrnPattern = Regex("soundcloud:users:(?<urn>\\d+)"),
            streamErrors = mapOf(
                "401" to "Invalid ClientID",
                "404" to "Track not found/requested private track"
            )
        ),
        apple = Regex(
            "https://music.apple.com/(?:.+)?(artist|album|music-video|playlist)/([\\w\\-.]+(?:/+[\\w\\-.]+|[^&]+))/?([\\w\\-.]+(?:/+[\\w\\-.]+|[^&]+))?"
        ),
        nico = Regex(
            "^https?://(?:www.|secure.|sp.)?nicovideo.jp/watch/([a-z]{2}[0-9]+)$"
        )
    )

    fun checkAllRegex(input: String, obj: Any, parentPath: String = ""): Map<String, MatchResult> {
        val results = mutableMapOf<String, MatchResult>()
        obj::class.memberProperties.forEach { prop ->
            if (prop.visibility == KVisibility.PUBLIC) { // public 프로퍼티만 처리
                val propValue = prop.getter.call(obj)
                val currentPath = if (parentPath.isEmpty()) prop.name else "$parentPath.${prop.name}"

                when (propValue) {
                    is Regex -> {
                        propValue.find(input)?.let { match ->
                            results[currentPath] = match
                        }
                    }
                    // 중첩 객체 (예: YoutubeRegexConfig) 처리
                    else -> if (propValue != null && propValue !is Collection<*> && propValue !is Map<*, *>) {
                        results += checkAllRegex(input, propValue, currentPath)
                    }
                }
            }
        }
        return results
    }
}