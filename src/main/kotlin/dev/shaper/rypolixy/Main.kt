package dev.shaper.rypolixy

import dev.shaper.rypolixy.config.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.kord.core.Kord
import dev.shaper.rypolixy.config.Configs
import dev.shaper.rypolixy.config.Settings


val logger = KotlinLogging.logger {}
val tokens = Configs.KEY.discord

suspend fun main() {
    Settings.errorHandler()
//    SoundcloudScrapper.findRelated("1199207923")!!.forEach {
//        logger.info { it.title }
//    }
    val kord = Kord(tokens)
    val bot = Client(logger, kord)
    bot
        .registerEvents()
        .registerCommands()
        .registerDatabase()
        .login()

}