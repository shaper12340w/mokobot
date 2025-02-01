package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.logger

object Configs {

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

    val KEY = KeyConfig(
        discord     = Properties.getProperty("discord.key")     ?: warn("discord.key"),
        youtube     = Properties.getProperty("youtube.key")     ?: debug("youtube.key"),
        soundcloud  = Properties.getProperty("soundcloud.key")  ?: debug("soundcloud.key"),
        spotify     = Properties.getProperty("spotify.key")     ?: warn("spotify.key"),
        artiva      = Properties.getProperty("artiva.key")      ?: warn("artiva.key")
    )

    val ID = IdConfig(
        discord =   Properties.getProperty("discord.client.id")         ?: warn("client.id"),
        server  =   Properties.getProperty("discord.guild.id")          ?: warn("guild.id"),
        spotify =  Properties.getProperty("spotify.id")                 ?: warn("spotify.key"),
    )
}