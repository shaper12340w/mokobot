package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.logger

object Settings {

    fun errorHandler(){
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            logger.error { "Uncaught exception in thread ${thread.name}: ${exception.message}" }
            exception.stackTrace.forEach {
                logger.error { "\t\t$it" }
            }
        }
    }

}