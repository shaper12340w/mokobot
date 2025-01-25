package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.logger
import kotlinx.coroutines.*

object Settings {

    private fun printException(thread: Thread?,exception: Throwable) {
        logger.error { "Uncaught exception ${if(thread != null) "in thread" else ""} ${thread?.name}: ${exception.message}" }
        exception.stackTrace.forEach {
            logger.error { "\t\t$it" }
        }
    }

    fun errorHandler(){
        Thread.setDefaultUncaughtExceptionHandler { thread, exception -> printException(thread,exception) }
        CoroutineExceptionHandler { _, exception -> printException(null,exception) }

    }

}