package dev.shaper.rypolixy.utils.discord.context

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.interaction.response.DeferredMessageInteractionResponseBehavior

sealed class DeferResponse {
    data class Interaction(val res: DeferredMessageInteractionResponseBehavior) : DeferResponse()
    data class Message(val res: MessageBehavior) : DeferResponse()
}