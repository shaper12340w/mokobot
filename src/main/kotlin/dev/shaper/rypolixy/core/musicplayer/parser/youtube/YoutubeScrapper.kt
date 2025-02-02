package dev.shaper.rypolixy.core.musicplayer.parser.youtube

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.core.musicplayer.utils.MediaRegex
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object YoutubeScrapper {

    /**
     * URL에 HTTP GET 요청을 보내고 응답 본문(HTML)을 문자열로 반환합니다.
     */
    private fun fetchUrl(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    /**
     * HTML 내에 포함된 ytInitialData JSON 데이터를 파싱합니다.
     *
     * 두 가지 패턴(데스크톱과 모바일)을 시도하며, 첫 번째로 매칭된 패턴의 JSON 문자열을 JSONObject로 변환하여 반환합니다.
     * 매칭되지 않으면 빈 JSONObject를 반환합니다.
     */
    private fun parseInitialData(html: String): JSONObject {
        // 중괄호를 이스케이프 처리한 일반 문자열 리터럴 사용
        val patterns = listOf(
            "var ytInitialData\\s*=\\s*(\\{.*?\\});</script>",
            "ytInitialData\\s*=\\s*(\\{.*?\\});</script>"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.DOT_MATCHES_ALL)
            val match = regex.find(html)
            if (match != null && match.groupValues.size >= 2) {
                val jsonStr = match.groupValues[1]
                return JSONObject(jsonStr)
            }
        }
        return JSONObject()
    }

    /**
     * HTML에서 관련 동영상(related videos)에 해당하는 결과를 추출한 후,
     * 최종 결과를 문자열(String)로 반환합니다.
     *
     * JSON 내의 "contents" → "twoColumnWatchNextResults" → "secondaryResults" →
     * "secondaryResults" → "results" 경로에 위치한 데이터를 문자열로 반환합니다.
     */
    private fun extractRelatedVideosAsString(html: String): String {
        val data = parseInitialData(html)
        val contents = data.optJSONObject("contents") ?: JSONObject()
        val twoColumnWatchNextResults = contents.optJSONObject("twoColumnWatchNextResults") ?: JSONObject()
        val secondaryResults = twoColumnWatchNextResults.optJSONObject("secondaryResults") ?: JSONObject()
        val secondaryResults2 = secondaryResults.optJSONObject("secondaryResults") ?: JSONObject()
        val resultsArray: JSONArray? = secondaryResults2.optJSONArray("results")
        return resultsArray?.toString() ?: "[]"
    }

    suspend fun getRelatedData(videoId: String,count:Int = 10): List<dev.shaper.rypolixy.core.musicplayer.parser.youtube.YoutubeParseInfo.CompactVideoInfo>? {
        try {
            return withTimeout(60000L) {
                val url  = "${MediaRegex.REGEX.youtube.youtubeBaseUrl}/watch?v=$videoId"
                val html = fetchUrl(url)
                val relatedVideosString = extractRelatedVideosAsString(html)
                if (relatedVideosString == "[]")
                    throw YoutubeDataException("result is empty")

                val moshi = Moshi.Builder()
                    .add(RelatedVideosAdapter())
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val listType = Types.newParameterizedType(List::class.java, YoutubeParseInfo.CompactVideoInfo::class.java)
                val adapter = moshi.adapter<List<YoutubeParseInfo.CompactVideoInfo>>(listType)
                return@withTimeout (adapter.fromJson(relatedVideosString) as List<YoutubeParseInfo.CompactVideoInfo>).toList().take(count)
            }

        }
        catch (e: TimeoutCancellationException) {
            logger.error { "Timeout while getting Youtube data from id : $videoId" }
            return null
        }
        catch (e: YoutubeDataException){
            logger.error { "Cannot get data from id : $videoId" }
            logger.error { e.message }
            return null
        }
        catch (e: Exception) {
            logger.error { "Cannot get data from id : $videoId" }
            logger.error { e.message }
            return null
        }

    }

}