package dev.shaper.rypolixy

import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.config.Properties
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.kord.core.Kord
import dev.shaper.rypolixy.config.Settings
import dev.shaper.rypolixy.utils.io.json.JsonManager
import dev.shaper.rypolixy.utils.musicplayer.ytdlp.YtDlpManager


val tokens = Properties.getProperty("discord.token")
val logger = KotlinLogging.logger {}

suspend fun main(){

    Settings.errorHandler()
    if(false){
        YtDlpManager.getPlaylistData("https://www.youtube.com/watch?v=fE9trKOuT3Q")
            .forEach { println(JsonManager.decode(it)) }
    }
    else{
        val kord = Kord(tokens)
        val bot = Client(logger, kord)
        bot
            .registerEvents()
            .registerCommands()
            .registerDatabase()
            .login()
    }

}