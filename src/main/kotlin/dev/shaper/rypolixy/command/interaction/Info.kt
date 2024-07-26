package dev.shaper.rypolixy.command.interaction

import dev.shaper.rypolixy.config.Client
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

class Info(private val client: Client): InteractionCommand() {

    override val name       : String
        get() = "info"
    override val description: String
        get() = "get system info"

    override fun setup(builder: ChatInputCreateBuilder){

    }

    override suspend fun execute(context: ChatInputCommandInteractionCreateEvent) {

    }


}