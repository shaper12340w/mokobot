package dev.shaper.rypolixy.core.musicplayer.parser

import dev.shaper.rypolixy.core.musicplayer.MediaTrack
import dev.shaper.rypolixy.core.musicplayer.parser.soundcloud.SoundcloudScrapper
import dev.shaper.rypolixy.core.musicplayer.parser.youtube.YoutubeScrapper
import dev.shaper.rypolixy.core.musicplayer.utils.MediaRegex
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import dev.shaper.rypolixy.logger
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object MediaParser {

    private fun parseTimeToSeconds(timeString: String): Int? {
        val parts = timeString.split(":").map { it.toIntOrNull() }

        // 모든 부분이 숫자이고 유효한지 확인
        if (parts.any { it == null || it < 0 }) return null

        return when (parts.size) {
            // 분:초 (예: "55:30" → 55*60 + 30 = 3330초)
            2 -> parts[0]!! * 60 + parts[1]!!

            // 시:분:초 (예: "1:03:02" → 1*3600 + 3*60 + 2 = 3782초)
            3 -> parts[0]!! * 3600 + parts[1]!! * 60 + parts[2]!!

            else -> null // 형식 오류
        }
    }

    suspend fun parse(track:MediaTrack.Track,count:Int = 5):List<MediaTrack>? {
        return when(track.source){
            MediaUtils.MediaPlatform.YOUTUBE -> {
                YoutubeScrapper.getRelatedData(track.id, count)?.map {
                    MediaTrack.FlatTrack(
                        title       = it.videoRenderer.title.text,
                        duration    = parseTimeToSeconds(it.videoRenderer.lengthText.text)?.toDuration(DurationUnit.SECONDS) ?: (0).toDuration(DurationUnit.SECONDS),
                        url         = "${MediaRegex.REGEX.youtube.youtubeBaseUrl}/watch?v=${it.videoRenderer.videoId}",
                        thumbnail   = it.videoRenderer.thumbnail.thumbnails.firstOrNull()?.url ?: "",
                        source      = track.source
                    )
                }
            }
            MediaUtils.MediaPlatform.SOUNDCLOUD -> {
                //TODO : extract track ID
                val regex = Regex("tracks:(\\d+)")
                val trackId = regex.find(track.id)?.value?.split(":")?.get(1) ?: throw IllegalArgumentException("Unknown track")
                SoundcloudScrapper.findRelated(trackId, count)?.map {
                    MediaTrack.FlatTrack(
                        title       = it.title,
                        duration    = it.duration.toDuration(DurationUnit.SECONDS),
                        url         = it.permalinkUrl,
                        source      = track.source,
                        thumbnail   = it.artworkUrl
                    )
                }
            }
            else -> throw IllegalArgumentException("Unsupported media platform")
        }
    }
}