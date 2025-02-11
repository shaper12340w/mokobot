package dev.shaper.rypolixy.utils.structure

import java.time.Instant
import java.util.LinkedHashMap

class CacheSystem<K,V>(
    private val maxSize: Int, // Cache's Max Size
    private val lifeTime: Long // Max lifetime of values (seconds)
) {

    private val cache: LinkedHashMap<K, Pair<V, Long>>
        = object : LinkedHashMap<K, Pair<V, Long>>(maxSize, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, Pair<V, Long>>): Boolean {
                // 캐시 크기가 최대 크기를 초과하면 가장 오래된 항목 삭제
                return size > maxSize
        }
    }

    // 변수를 캐시에 추가
    fun put(key: K, value: V, lifetime: Long = lifeTime) {
        val expiryTime = Instant.now().epochSecond + lifetime
        cache[key] = value to expiryTime
    }

    // 변수를 캐시에서 가져오기
    fun get(key: K, fetchNewValue: () -> V): V {
        val currentTime = Instant.now().epochSecond
        val cachedValue = cache[key]

        return if (cachedValue != null && cachedValue.second > currentTime) {
            // 캐시가 유효한 경우
            cachedValue.first
        } else {
            // 캐시가 만료되었거나 없는 경우, 새로운 값을 가져옴
            val newValue = fetchNewValue()
            put(key, newValue)
            newValue
        }
    }

    // 캐시에서 변수 제거
    fun remove(key: K) {
        cache.remove(key)
    }

    // 캐시 초기화
    fun clear() {
        cache.clear()
    }


}