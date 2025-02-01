package dev.shaper.rypolixy

import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.config.Properties
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.kord.core.Kord
import dev.shaper.rypolixy.config.Configs
import dev.shaper.rypolixy.config.Settings
import dev.shaper.rypolixy.utils.musicplayer.parser.youtube.YoutubeScrapper

val tokens = Configs.KEY.discord
val logger = KotlinLogging.logger {}

suspend fun main() {

    Settings.errorHandler()
    val kord = Kord(tokens)
    val bot = Client(logger, kord)
    bot
        .registerEvents()
        .registerCommands()
        .registerDatabase()
        .login()

}