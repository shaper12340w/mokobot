package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.logger

object Configs {

    data class DBConfig(
        val url: String,
        val name: String,
        val username: String,
        val password: String,
    )

    data class KeyConfig(
        var discord: String,
        var youtube: String,
        var soundcloud: String,
        var spotify: String,
        var artiva: String
    )

    data class IdConfig(
        var discord: String,
        var spotify: String,
        var server: String
    )

    data class ProgramConfig(
        var ytdlp: String,
    )

    data class AppConfig(
        var register: String,
    )

    /*
    * If setting variable is important
    * */
    private fun warn(property: String):String{
        logger.warn { "Missing environment variable: $property" }
        return ""
    }

    /*
    * If setting variable is not important
    * */
    private fun debug(property: String):String{
        logger.debug { "Missing environment variable: $property" }
        return ""
    }

    val DB = DBConfig(
        url         = Properties.getProperty("db.url")      ?: warn("db.url"),
        name        = Properties.getProperty("db.name")     ?: warn("db.name"),
        username    = Properties.getProperty("db.user")     ?: warn("db.user"),
        password    = Properties.getProperty("db.password") ?: warn("db.password")
    )

    val KEY = KeyConfig(
        discord     = Properties.getProperty("discord.key")     ?: warn("discord.key"),
        youtube     = Properties.getProperty("youtube.key")     ?: debug("youtube.key"),
        soundcloud  = Properties.getProperty("soundcloud.key")  ?: debug("soundcloud.key"),
        spotify     = Properties.getProperty("spotify.key")     ?: warn("spotify.key"),
        artiva      = Properties.getProperty("artiva.key")      ?: warn("artiva.key")
    )

    val ID = IdConfig(
        discord = Properties.getProperty("discord.client.id")         ?: debug("client.id"), //can get from kord
        server  = Properties.getProperty("discord.guild.id")          ?: debug("guild.id"),  //not essential
        spotify = Properties.getProperty("spotify.id")                ?: warn("spotify.id"),
    )

    val PROGRAMS = ProgramConfig(
        ytdlp   = when(System.getProperty("os.name")){
            "Linux"         -> Properties.getProperty("program.linux.ytdlp")    ?: warn("program.linux.ytdlp")
            "Windows 11"    -> Properties.getProperty("program.windows.ytdlp")  ?: warn("program.windows.ytdlp")
            else            -> warn("program.unknown.ytdlp")
        }
    )

    val SETTINGS = AppConfig(
        register = Properties.getProperty("app.register")               ?: warn("app.register"),
    )
}