package dev.shaper.rypolixy.command.types

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

sealed class ContextType {
    data class Interaction  (val value: ChatInputCommandInteractionCreateEvent)   : ContextType()
    data class Message      (val value: MessageCreateEvent)                       : ContextType()
}