package dev.shaper.rypolixy.core.music.parser.soundcloud

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.core.music.utils.MediaRegex
import dev.shaper.rypolixy.utils.io.file.CacheFile
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream

object SoundcloudScrapper {
    private val client = OkHttpClient()

    /**
     * URL에 GET 요청을 보내고 응답 본문(HTML)을 문자열로 반환합니다.
     */
    @Throws(IOException::class)
    fun fetchUrl(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body?.string() ?: ""
        }
    }

    /**
     * 주어진 URL의 HTML을 가져옵니다.
     * options를 추가로 사용하려면 OkHttp Request.Builder에 적용하세요.
     */
    private fun parseHtml(url: String): String {
        return try {
            // options 적용 예시: 헤더 추가 등 (프로젝트에 맞게 수정)
            fetchUrl(url)
        } catch (e: Exception) {
            logger.error { e.message }
            ""
        }
    }

    /**
     * 키를 생성하는 메소드
     * SoundCloud의 키를 얻기 위해 BASE_URL의 HTML에서 스크립트 URL을 추출한 후,
     * 해당 URL에서 client_id 값을 파싱합니다.
     */
    private fun keygen(): String? {
        try {
            val baseUrl = MediaRegex.REGEX.soundcloud.soundcloudBaseUrl
            val baseRegex = Regex("^https?://(www.)?[-a-zA-Z0-9@:%._+~#=]{1,256}.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$")
            val html = parseHtml(baseUrl)
            val parts = html.split("<script crossorigin src=\"")
            val urls = mutableListOf<String>()
            parts.forEach { part ->
                val chunk = part.substringBefore("\n").substringBefore("\"></script>")
                if (baseRegex.containsMatchIn(chunk)) {
                    urls.add(chunk)
                }
            }
            var key: String? = null
            var index = 0
            while (index < urls.size && key == null) {
                val url = urls[index]
                index++
                if (baseRegex.containsMatchIn(url)) {
                    val data = parseHtml(url)
                    if (data.contains(",client_id:\"")) {
                        val a = data.split(",client_id:\"")
                        key = a.getOrNull(1)?.substringBefore("\"")
                    }
                }
            }
            return key
        } catch (e: Exception) {
            logger.error { e }
            return null
        }
    }

    private fun getKey(): String {
        val load = CacheFile.loadKey("soundcloud_key")
        if (load != null)
            return load
        else{
            val key = keygen()!!
            CacheFile.saveKey("soundcloud_key", key)
            return key
        }
    }


    /**
     * 주어진 트랙 ID에 대해 관련 트랙 정보를 SoundCloudTrack 리스트로 반환합니다.
     */
     suspend fun findRelated(trackId: String,limit:Int = 10): List<SoundcloudParseInfo.SoundCloudTrack>? {
        try{
            return withTimeout(60000L) {
                logger.debug { "Generated new Soundcloud Key : ${getKey()}" }
                val baseUrl = "https://api-v2.soundcloud.com/tracks/$trackId/related"
                val httpUrlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return@withTimeout null

                httpUrlBuilder.apply {
                    addQueryParameter("client_id", getKey())
                    addQueryParameter("limit", limit.toString())
                    addQueryParameter("offset", "0")
                    addQueryParameter("linked_partitioning", "1")
                    addQueryParameter("app_version", "1689322736")
                    addQueryParameter("app_locale", "en")
                }
                val finalUrl = httpUrlBuilder.build().toString()
                val request = Request.Builder()
                    .url(finalUrl)
                    .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                    .addHeader("Accept-Encoding", "gzip, deflate, br")
                    .addHeader("Accept-Language", "ko,en;q=0.9,en-US;q=0.8")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Host", "api-v2.soundcloud.com")
                    .addHeader("Origin", "https://soundcloud.com")
                    .addHeader("Referer", "https://soundcloud.com/")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-site")
                    .addHeader(
                        "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.82"
                    )
                    .addHeader("sec-ch-ua", "\"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Microsoft Edge\";v=\"114\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        logger.error { IOException("Unexpected code $response") }
                        return@withTimeout null
                    }
                    val responseBody = response.body!!.bytes()
                    val inputStream = GZIPInputStream(ByteArrayInputStream(responseBody))
                    val decompressed = inputStream.bufferedReader().readText()
                    logger.debug { "Returned Data : $decompressed" }
                    // Moshi를 사용해 JSON을 파싱합니다.
                    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    val adapter = moshi.adapter(SoundcloudParseInfo.RelatedResponse::class.java)
                    val relatedResponse = adapter.fromJson(decompressed)
                    return@withTimeout relatedResponse?.collection
                }
            }
        } catch (e: TimeoutCancellationException) {
            logger.error { "Timeout while getting SoundCloud data from id : $trackId" }
            return null
        } catch (e: Exception) {
            logger.error { "Cannot get data from id : $trackId" }
            logger.error { e.message }
            return null
        }

    }
}