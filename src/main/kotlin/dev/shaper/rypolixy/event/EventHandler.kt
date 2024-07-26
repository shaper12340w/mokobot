package dev.shaper.rypolixy.event

import dev.shaper.rypolixy.config.Client
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

class EventHandler(private val client: dev.shaper.rypolixy.config.Client) {

    suspend fun onMessageEvent(event: MessageCreateEvent){
        client.commandManager.executeTextCommand(event)
    }

    suspend fun onReadyEvent(event: ReadyEvent){
        val username = event.kord.getSelf().username
        client.logger.info { "Logged in as $username" }
    }

    suspend fun onCommandInteraction(event: GuildChatInputCommandInteractionCreateEvent){

    }


}