package dev.shaper.rypolixy

import dev.shaper.rypolixy.config.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.kord.core.Kord
import dev.shaper.rypolixy.config.Configs
import dev.shaper.rypolixy.config.Settings

val logger = KotlinLogging.logger {}
val config = Configs().loadConfig()

suspend fun main() {

    Settings.apply {
        errorHandler()
        printHandler()
    }

    val kord = Kord(config.auth.key.discord)
    val bot = Client(logger, kord)
    bot.apply {
        registerEvents()
        registerCommands()
        registerDatabase()
        login()
    }

}