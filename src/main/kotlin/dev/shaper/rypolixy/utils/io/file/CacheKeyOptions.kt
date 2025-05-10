package dev.shaper.rypolixy.utils.io.file

import java.io.Serializable

data class CacheKeyOptions(
    val generateDate: Long = 0,
    val expireAfter: Long = 0,
    val key: String
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}