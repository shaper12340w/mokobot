package dev.shaper.rypolixy.command.types

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

interface MutualCommand: TextCommand,InteractionCommand {

    val isInteractive: Boolean get() = false

    suspend fun execute(context: ContextType, res: TextCommand.ResponseData?)

    override suspend fun execute(event: MessageCreateEvent, res: TextCommand.ResponseData?) {}

    override suspend fun execute(context: ChatInputCommandInteractionCreateEvent) {}

}