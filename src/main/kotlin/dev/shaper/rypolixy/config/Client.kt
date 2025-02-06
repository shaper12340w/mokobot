package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.event.EventHandler
import dev.kord.core.Kord
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.shaper.rypolixy.command.types.CommandManager
import dev.shaper.rypolixy.core.musicplayer.MediaPlayer
import dev.shaper.rypolixy.utils.io.database.Database
import io.github.oshai.kotlinlogging.KLogger

@OptIn(PrivilegedIntent::class)
class Client(internal val logger: KLogger, internal val kord:Kord) {

    val commandManager: CommandManager = CommandManager(this)
    val lavaClient: MediaPlayer = MediaPlayer()

    init {
        logger.info {"Bot started"}
    }

    suspend fun login() {
        logger.info { "Logging in" }
        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents {
                +Intent.Guilds
                +Intent.GuildInvites
                +Intent.GuildMessages
                +Intent.GuildMessageReactions
                +Intent.GuildMembers
                +Intent.GuildPresences
                +Intent.GuildVoiceStates
                +Intent.MessageContent
            }
        }
    }

    fun registerEvents() = apply {
        val handler = EventHandler(this)
        kord.on(kord,handler::onReadyEvent)
        kord.on(kord,handler::onMessageEvent)
        kord.on(kord,handler::onCommandInteraction)
    }

    suspend fun registerCommands() = apply {
        Register.register(this)
    }

    fun registerDatabase() = apply {
        Database.initTable()
    }

}