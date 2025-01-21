package dev.shaper.rypolixy.utils.musicplayer

class AudioType {
    enum class Link(
        val url: List<String>,
        val icon: String
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
            "https://www.youtube.com/favicon.ico"
        ),
        SOUNDCLOUD(
            listOf(
                "/^https?:\\/\\/(soundcloud\\.com|snd\\.sc)\\/(.*)\$/",
                "^(?:https?:\\/\\/)((?:www\\.)|(?:m\\.))?soundcloud\\.com\\/[a-z0-9](?!.*?(-|_){2})[\\w-]{1,23}[a-z0-9](?:\\/.+)?\$",
                "/^https?:\\/\\/(soundcloud\\.com|snd\\.sc)\\/([A-Za-z0-9_-]+)\\/([A-Za-z0-9_-]+)\\/?\$/",
            ),
            "https://soundcloud.com"
        )

    }
}