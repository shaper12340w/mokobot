package dev.shaper.rypolixy.event

import dev.shaper.rypolixy.config.Client
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildModalSubmitInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.actionrow.ActionRowManager
import dev.shaper.rypolixy.utils.io.database.Database
import dev.shaper.rypolixy.utils.io.database.DatabaseManager

class EventHandler(private val client: Client) {

    suspend fun onMessageEvent(event: MessageCreateEvent){
        client.commandManager.executeTextCommand(event)
    }

    suspend fun onReadyEvent(event: ReadyEvent){
        val username = event.kord.getSelf().username
        client.logger.info { "[Ready] : Logged in as $username" }
    }

    suspend fun onCommandInteraction(event: GuildChatInputCommandInteractionCreateEvent){
        client.commandManager.interactionCommand[event.interaction.command.rootName]?.execute(event)
        client.commandManager.mutualCommand[event.interaction.command.rootName]?.execute(ContextType.Interaction(event),null)
        logger.info { "[Interaction][ KEY : ${event.interaction.command.rootName} ] (guildId : ${event.interaction.guildId} | channelId : ${event.interaction.channelId})" }
    }

    suspend fun onGuildCreate(event: GuildCreateEvent){
        try {
            if(DatabaseManager.fetchGuildUUID(event.guild.id) == null)
                DatabaseManager.registerAll(event.guild)
        } catch (e:Exception){
            Database.initGuild(event.guild)
            DatabaseManager.registerAll(event.guild)
        }
    }

    fun onMemberJoin(event: MemberJoinEvent){
        Database.initUser(event.member)
    }

    fun onButtonInteraction(event: ButtonInteractionCreateEvent){
        ActionRowManager.emitter.emit(ActionRowManager.ButtonEvent(
            event.interaction.component.customId!!,
            event.interaction
        ))
        logger.info { "[Button] : Successfully executed with id ${event.interaction.component.customId} / (guildId : ${event.interaction.data.guildId.value} | channelId : ${event.interaction.channelId})" }
    }

    fun onModalSubmitInteraction(event: GuildModalSubmitInteractionCreateEvent) {
        ActionRowManager.emitter.emit(ActionRowManager.ModalEvent(
            event.interaction.modalId,
            event.interaction
        ))
        logger.info { "[Button] : Successfully executed with id ${event.interaction.modalId} / (guildId : ${event.interaction.data.guildId} | channelId : ${event.interaction.channelId})" }
    }

    fun onSelectMenuInteraction(event: SelectMenuInteractionCreateEvent){
        ActionRowManager.emitter.emit(ActionRowManager.SelectMenuEvent(
            event.interaction.component.customId,
            event.interaction
        ))
        logger.info { "[SelectMenu] : Successfully executed with id ${event.interaction.component.customId} / (guildId : ${event.interaction.data.guildId.value} | channelId : ${event.interaction.channelId})" }
    }

}