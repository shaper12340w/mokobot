package dev.shaper.rypolixy.event

import dev.shaper.rypolixy.config.Client
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.shaper.rypolixy.command.types.ContextType

class EventHandler(private val client: Client) {

    suspend fun onMessageEvent(event: MessageCreateEvent){
        client.commandManager.executeTextCommand(event)
    }

    suspend fun onReadyEvent(event: ReadyEvent){
        val username = event.kord.getSelf().username
        client.logger.info { "Logged in as $username" }
    }

    suspend fun onCommandInteraction(event: GuildChatInputCommandInteractionCreateEvent){
        client.commandManager.interactionCommand[event.interaction.command.rootName]?.execute(event)
        client.commandManager.mutualCommand[event.interaction.command.rootName]?.execute(ContextType.Interaction(event),null)
    }


}