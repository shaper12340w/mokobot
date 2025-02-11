package dev.shaper.rypolixy.utils.structure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RetryUtil {

    suspend fun <T> retry(
        retries: Int = 3,
        initialDelay: Long = 100, // 초기 지연 시간 (ms)
        maxDelay: Long = 1000, // 최대 지연 시간 (ms)
        block: suspend () -> T
    ): T {
        var currentRetry = 0
        var currentDelay = initialDelay

        while (true) {
            try {
                return block()
            } catch (e: Exception) {
                if (currentRetry++ >= retries) {
                    throw e // 재시도 횟수를 초과하면 예외를 던짐
                }
                println("Retry $currentRetry after $currentDelay ms due to: ${e.message}")
                withContext(Dispatchers.IO) {
                    Thread.sleep(currentDelay)
                }
                currentDelay = (currentDelay * 2).coerceAtMost(maxDelay) // 지수 백오프
            }
        }
    }


    suspend fun <T> retryOnSpecificException(
        retries: Int = 3,
        initialDelay: Long = 100,
        maxDelay: Long = 1000,
        exceptionPredicate: (Throwable) -> Boolean = { true }, // 재시도할 예외를 필터링
        block: suspend () -> T
    ): T {
        var currentRetry = 0
        var currentDelay = initialDelay

        while (true) {
            try {
                return block()
            } catch (e: Exception) {
                if (!exceptionPredicate(e) || currentRetry++ >= retries) {
                    throw e
                }
                println("Retry $currentRetry after $currentDelay ms due to: ${e.message}")
                withContext(Dispatchers.IO) {
                    Thread.sleep(currentDelay)
                }
                currentDelay = (currentDelay * 2).coerceAtMost(maxDelay)
            }
        }
    }

}