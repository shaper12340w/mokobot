package dev.shaper.rypolixy

import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.config.Properties
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.kord.core.Kord
import dev.shaper.rypolixy.config.Settings
import dev.shaper.rypolixy.utils.io.json.JsonManager
import dev.shaper.rypolixy.utils.musicplayer.ytdlp.YtDlpInfo
import dev.shaper.rypolixy.utils.musicplayer.ytdlp.YtDlpManager


val tokens = Properties.getProperty("discord.token")
val logger = KotlinLogging.logger {}

suspend fun main(){

    Settings.errorHandler()
    if(true){
        val data = YtDlpManager.getData("https://www.youtube.com/watch?v=WwuuVWNgUq8")
        val result = when(data){
            is YtDlpInfo.TrackInfo          -> JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.TrackInfo::class).encode(data)
            is YtDlpInfo.PlaylistInfo       -> JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.PlaylistInfo::class).encode(data)
            is YtDlpInfo.FlatPlaylistInfo   -> JsonManager().sealedBuilder(YtDlpInfo::class,YtDlpInfo.FlatPlaylistInfo::class).encode(data)
            else -> null
        }
        logger.debug { "result: $result" }
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