package dev.shaper.rypolixy.core.music.parser

import dev.shaper.rypolixy.core.music.MediaTrack
import dev.shaper.rypolixy.core.music.parser.soundcloud.SoundcloudScrapper
import dev.shaper.rypolixy.core.music.parser.youtube.YoutubeScrapper
import dev.shaper.rypolixy.core.music.utils.MediaRegex
import dev.shaper.rypolixy.core.music.utils.MediaUtils
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object MediaParser {

    private fun parseTimeToSeconds(timeString: String): Int? {
        val parts = timeString.split(":").map { it.toIntOrNull() }

        if (parts.any { it == null || it < 0 }) return null

        return when (parts.size) {
            2 -> parts[0]!! * 60 + parts[1]!!
            3 -> parts[0]!! * 3600 + parts[1]!! * 60 + parts[2]!!
            else -> null
        }
    }

    suspend fun parse(track: MediaTrack.Track,count: Int = 5):List<MediaTrack>? = parse(track.id,track.source)
    suspend fun parse(id:String,source:MediaUtils.MediaPlatform,count:Int = 5):List<MediaTrack>? {
        return when(source){
            MediaUtils.MediaPlatform.YOUTUBE -> {
                YoutubeScrapper.getRelatedData(id, count)?.map {
                    MediaTrack.FlatTrack(
                        title       = it.videoRenderer.title.text,
                        duration    = parseTimeToSeconds(it.videoRenderer.lengthText.text)?.toDuration(DurationUnit.SECONDS) ?: (0).toDuration(DurationUnit.SECONDS),
                        url         = "${MediaRegex.REGEX.youtube.youtubeBaseUrl}/watch?v=${it.videoRenderer.videoId}",
                        thumbnail   = it.videoRenderer.thumbnail.thumbnails.firstOrNull()?.url ?: "",
                        source      = source,
                        artist      = it.videoRenderer.channelInfo.runs.firstOrNull()?.channelName ?: "Unknown"
                    )
                }
            }
            MediaUtils.MediaPlatform.SOUNDCLOUD -> {
                val regex = Regex("tracks:(\\d+)")
                val trackId = regex.find(id)?.value?.split(":")?.get(1) ?: throw IllegalArgumentException("Unknown track")
                SoundcloudScrapper.findRelated(trackId, count)?.map {
                    MediaTrack.FlatTrack(
                        title       = it.title,
                        duration    = it.duration.toDuration(DurationUnit.SECONDS),
                        url         = it.permalinkUrl,
                        source      = source,
                        thumbnail   = it.artworkUrl,
                        artist      = it.publisherMetadata?.artist ?: "Unknown"
                    )
                }
            }
            else -> throw IllegalArgumentException("Unsupported media platform")
        }
    }
}