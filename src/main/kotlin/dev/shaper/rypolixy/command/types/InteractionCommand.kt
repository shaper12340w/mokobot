package dev.shaper.rypolixy.command.types

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

interface InteractionCommand: CommandStructure {

    override val enabled: Boolean?

    fun setup(builder: ChatInputCreateBuilder) {}

    suspend fun execute(context: ChatInputCommandInteractionCreateEvent)

}