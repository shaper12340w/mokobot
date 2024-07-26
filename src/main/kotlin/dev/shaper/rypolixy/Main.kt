package dev.shaper.rypolixy

import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.config.Properties
import dev.kord.core.Kord

val tokens = Properties.getProperty("discord.token")
val logger = mu.KotlinLogging.logger {}

suspend fun main(){

    val kord = Kord(tokens)
    val bot = Client(logger, kord)
    bot
        .registerEvents()
        .registerCommands()
        .registerDatabase()
        .login()

}