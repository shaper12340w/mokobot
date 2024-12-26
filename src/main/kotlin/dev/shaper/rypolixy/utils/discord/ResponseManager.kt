package dev.shaper.rypolixy.utils.discord

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.interaction.response.*
import dev.kord.core.behavior.reply
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.modify.AbstractMessageModifyBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import dev.kord.rest.builder.message.modify.UserMessageModifyBuilder
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond


class ResponseManager{

    companion object{

        suspend fun InteractionResponseBehavior.sendRespond(embed: EmbedBuilder) {
            sendRespond {
                embeds = mutableListOf(embed)
            }
        }

        suspend fun InteractionResponseBehavior.sendRespond(block: InteractionResponseModifyBuilder.()->Unit) {
            when(this){
                is MessageInteractionResponseBehavior -> this.edit(block)
                is DeferredMessageInteractionResponseBehavior -> this.respond(block)
                is FollowupPermittingInteractionResponseBehavior -> { return }
                else -> { return }
            }
        }

        suspend fun MessageBehavior.sendRespond(embed: EmbedBuilder) {
            this.reply { embeds = mutableListOf(embed) }
        }

        suspend fun MessageBehavior.sendRespond(block: UserMessageCreateBuilder.()->Unit) {
            this.reply { block() }
        }

        suspend fun ContextType.sendRespond(embed: EmbedBuilder) {
            sendRespond { embeds = mutableListOf(embed) }
        }

        suspend fun ContextType.sendRespond(block: AbstractMessageModifyBuilder.()->Unit) {
            when(this){
                is ContextType.Message -> this.sendRespond(block)
                is ContextType.Interaction -> this.sendRespond(block)
            }
        }

    }

}