package dev.shaper.rypolixy.command.message

import dev.shaper.rypolixy.command.CommandStructure
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

abstract class MessageCommand: CommandStructure() {

    open fun setup(builder: ChatInputCreateBuilder) {}

    abstract suspend fun execute(context: ChatInputCommandInteractionCreateEvent)

}