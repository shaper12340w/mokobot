package dev.shaper.rypolixy

import dev.shaper.rypolixy.config.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.kord.core.Kord
import dev.shaper.rypolixy.config.Configs
import dev.shaper.rypolixy.config.Settings
import dev.shaper.rypolixy.core.musicplayer.parser.soundcloud.SoundcloudScrapper


val logger = KotlinLogging.logger {}
val tokens = Configs.KEY.discord

suspend fun main() {

    Settings.apply {
        errorHandler()
        printHandler()
    }

    val kord = Kord(tokens)
    val bot = Client(logger, kord)
    bot.apply {
        registerEvents()
        registerCommands()
        registerDatabase()
        login()
    }

}