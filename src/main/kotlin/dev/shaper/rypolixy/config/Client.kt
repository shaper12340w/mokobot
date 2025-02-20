package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.event.EventHandler
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.GuildModalSubmitInteraction
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildModalSubmitInteractionCreateEvent
import dev.kord.core.event.interaction.GuildSelectMenuInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.shaper.rypolixy.command.types.CommandManager
import dev.shaper.rypolixy.core.musicplayer.MediaPlayer
import dev.shaper.rypolixy.utils.io.database.Database
import dev.shaper.rypolixy.utils.io.database.DatabaseManager
import io.github.oshai.kotlinlogging.KLogger

@OptIn(PrivilegedIntent::class)
class Client(internal val logger: KLogger, internal val kord:Kord) {

    val commandManager: CommandManager  = CommandManager(this)
    val lavaClient: MediaPlayer         = MediaPlayer()

    init {
        logger.info {"[Client] : Bot started"}
    }

    suspend fun login() {
        logger.info { "[Client] : Logging in" }
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
        kord.on<ReadyEvent>                                 (kord,handler::onReadyEvent)
        kord.on<MessageCreateEvent>                         (kord,handler::onMessageEvent)
        kord.on<GuildCreateEvent>                           (kord,handler::onGuildCreate)
        kord.on<MemberJoinEvent>                            (kord,handler::onMemberJoin)
        kord.on<ButtonInteractionCreateEvent>               (kord,handler::onButtonInteraction)
        kord.on<GuildModalSubmitInteractionCreateEvent>     (kord,handler::onModalSubmitInteraction)
        kord.on<GuildChatInputCommandInteractionCreateEvent>(kord,handler::onCommandInteraction)
        kord.on<GuildSelectMenuInteractionCreateEvent>      (kord,handler::onSelectMenuInteraction)
    }

    suspend fun registerCommands() = apply {
        Register.register(this)
    }

    fun registerDatabase() = apply {
        Database.checkOwner()
        Database.initTable()
        DatabaseManager.initValues()
    }

}