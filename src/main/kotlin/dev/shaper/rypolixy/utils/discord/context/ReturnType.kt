package dev.shaper.rypolixy.utils.discord.context

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.response.MessageInteractionResponse
import dev.kord.core.entity.Message as KordMessage

sealed class ReturnType {
    data class Interaction  (val data: MessageInteractionResponseBehavior) : ReturnType()
    data class Message      (val data: KordMessage)                 : ReturnType()
}