package dev.shaper.rypolixy.utils

import java.net.HttpURLConnection
import java.net.URL

object CheckNetwork {

    fun pingURL(ip: String): Long? {
        return try {
            val startTime   = System.currentTimeMillis()
            val connection  = URL(ip).openConnection() as HttpURLConnection
            connection.requestMethod    = "GET"
            connection.connectTimeout   = 5000 // 타임아웃 설정 (예: 5초)
            connection.readTimeout      = 5000 // 타임아웃 설정 (예: 5초)
            connection.connect()

            if (connection.responseCode == 200) {
                val endTime = System.currentTimeMillis()
                endTime - startTime
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
