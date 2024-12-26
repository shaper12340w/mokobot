package dev.shaper.rypolixy.command.types

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

sealed class ContextType {
    data class Interaction(val value: ChatInputCommandInteractionCreateEvent) : ContextType()
    data class Message(val value: MessageBehavior) : ContextType()
}