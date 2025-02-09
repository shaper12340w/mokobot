package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.logger
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream

object Settings {

    fun printException(thread: Thread?,exception: Throwable) {
        logger.error { "Uncaught exception ${if(thread != null) "in thread" else ""} ${thread?.name}: ${exception.message}" }
        exception.stackTrace.forEach {
            logger.error { "\t\t$it" }
        }
    }

    fun errorHandler(){
        Thread.setDefaultUncaughtExceptionHandler { thread, exception -> printException(thread,exception) }
        CoroutineExceptionHandler { _, exception -> printException(null,exception) }
    }

    fun printHandler() {
//        val loggingStream = object : PrintStream(ByteArrayOutputStream()) {
//            override fun println(x: Any?)
//                = logger.warn { x }
//
//        }
//        System.setOut(loggingStream)

    }
}