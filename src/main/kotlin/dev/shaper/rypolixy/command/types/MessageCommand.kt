package dev.shaper.rypolixy.command.types

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

interface MessageCommand: CommandStructure {

    fun setup(builder: ChatInputCreateBuilder) {}

    suspend fun execute(context: ChatInputCommandInteractionCreateEvent)

}