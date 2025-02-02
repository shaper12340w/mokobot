package dev.shaper.rypolixy.core.musicplayer.parser.youtube

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

class RelatedVideosAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): List<YoutubeParseInfo.CompactVideoInfo> {
        val videos = mutableListOf<YoutubeParseInfo.CompactVideoInfo>()
        reader.beginArray()
        while (reader.hasNext()) {
            try {
                val jsonValue = reader.readJsonValue()
                @Suppress("UNCHECKED_CAST")
                val item = jsonValue as? Map<String, Any>

                // compactVideoRenderer가 있는 항목만 처리
                if (item?.containsKey("compactVideoRenderer") == true) {
                    videos.add(parseVideoInfo(item))
                }
            } catch (e: Exception) {
                // 파싱 실패한 항목은 무시하고 계속 진행
                continue
            }
        }
        reader.endArray()
        return videos
    }

    private fun parseVideoInfo(item: Map<String, Any>): YoutubeParseInfo.CompactVideoInfo {
        val renderer = item["compactVideoRenderer"] as Map<String, Any>

        val videoId = renderer["videoId"] as String
        val title = (renderer["title"] as Map<String, Any>)["simpleText"] as String
        val lengthText = (renderer["lengthText"] as Map<String, Any>)["simpleText"] as String
        val viewCountText = (renderer["viewCountText"] as Map<String, Any>)["simpleText"] as String
        val publishedTimeText = (renderer["publishedTimeText"] as Map<String, Any>)["simpleText"] as String

        return YoutubeParseInfo.CompactVideoInfo(
            YoutubeParseInfo.RelatedVideo(
                videoId = videoId,
                title = YoutubeParseInfo.VideoTitle(title),
                lengthText = YoutubeParseInfo.SimpleText(lengthText),
                viewCountText = YoutubeParseInfo.SimpleText(viewCountText),
                publishedTimeText = YoutubeParseInfo.SimpleText(publishedTimeText),
                thumbnail = parseThumbnail(renderer["thumbnail"] as Map<String, Any>),
                channelInfo = parseChannelInfo(renderer["shortBylineText"] as Map<String, Any>)
            )
        )
    }

    private fun parseThumbnail(thumbnailData: Map<String, Any>): YoutubeParseInfo.Thumbnail {
        @Suppress("UNCHECKED_CAST")
        val thumbnails = (thumbnailData["thumbnails"] as List<Map<String, Any>>).map {
            YoutubeParseInfo.ThumbnailItem(
                url = it["url"] as String,
                width = (it["width"] as Number).toInt(),
                height = (it["height"] as Number).toInt()
            )
        }
        return YoutubeParseInfo.Thumbnail(thumbnails)
    }

    private fun parseChannelInfo(channelData: Map<String, Any>): YoutubeParseInfo.ShortBylineText {
        @Suppress("UNCHECKED_CAST")
        val runs = (channelData["runs"] as List<Map<String, Any>>).map {
            YoutubeParseInfo.ChannelRun(it["text"] as String)
        }
        return YoutubeParseInfo.ShortBylineText(runs)
    }
}