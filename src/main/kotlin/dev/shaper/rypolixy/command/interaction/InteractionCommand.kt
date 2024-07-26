package dev.shaper.rypolixy.command.interaction

import dev.shaper.rypolixy.command.CommandStructure
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

abstract class InteractionCommand: CommandStructure() {

    open fun setup(builder: ChatInputCreateBuilder) {}

    abstract suspend fun execute(context: ChatInputCommandInteractionCreateEvent)

}