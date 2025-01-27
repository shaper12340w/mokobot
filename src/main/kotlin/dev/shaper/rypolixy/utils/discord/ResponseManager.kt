package dev.shaper.rypolixy.utils.discord

import dev.kord.core.KordObject
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.*
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.Strategizable
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.MessageBuilder
import dev.shaper.rypolixy.command.types.ContextType


class ResponseManager{

    companion object{


        suspend fun InteractionResponseBehavior.sendRespond(embed: EmbedBuilder): ReturnType<InteractionResponseBehavior,Message>? = sendRespond(responseType = ResponseType.NORMAL,embed = embed)

        suspend fun InteractionResponseBehavior.sendRespond(responseType: ResponseType?,embed: EmbedBuilder): ReturnType<InteractionResponseBehavior,Message>?{
            return sendRespond(responseType) { embeds = mutableListOf(embed) }
        }

        suspend fun InteractionResponseBehavior.sendRespond(block: MessageBuilder.()->Unit): ReturnType<InteractionResponseBehavior,Message>? = sendRespond(responseType = ResponseType.NORMAL){ block() }

        suspend fun InteractionResponseBehavior.sendRespond(responseType: ResponseType?, block: MessageBuilder.()->Unit): ReturnType<InteractionResponseBehavior,Message>? {
            when(this){
                is MessageInteractionResponseBehavior           -> return ReturnType.Interaction(this.edit(block))
                is DeferredMessageInteractionResponseBehavior   -> {
                    if(this is ChatInputCommandInteractionCreateEvent){
                        when(responseType) {
                            ResponseType.NORMAL     -> return ReturnType.Interaction(this.respond(block))
                            ResponseType.EPHEMERAL  -> return ReturnType.Interaction(this.interaction.respondEphemeral(block))
                            ResponseType.NO_REPLY   -> return ReturnType.Message(this.interaction.channel.createMessage(block))
                            else -> return ReturnType.Interaction(this.respond(block))
                        }
                    }
                    return ReturnType.Interaction(this.respond(block))
                }
                is FollowupPermittingInteractionResponseBehavior -> {  }
                else -> {  }
            }
            return null
        }

        suspend fun MessageBehavior.sendRespond(embed: EmbedBuilder): ReturnType<InteractionResponseBehavior,Message>? = sendRespond(responseType = ResponseType.NORMAL,embed = embed)

        suspend fun MessageBehavior.sendRespond(responseType: ResponseType?,embed: EmbedBuilder):ReturnType<InteractionResponseBehavior,Message> {
            return ReturnType.Message(this.reply { embeds = mutableListOf(embed) })
        }

        suspend fun MessageBehavior.sendRespond(block: MessageBuilder.()->Unit):ReturnType<InteractionResponseBehavior,Message> {
            return ReturnType.Message( this.reply { block() } )
        }

        suspend fun ContextType.sendRespond(embed: EmbedBuilder): ReturnType<InteractionResponseBehavior,Message>? = sendRespond(responseType = ResponseType.NORMAL,embed = embed)

        suspend fun ContextType.sendRespond(responseType: ResponseType?,embed: EmbedBuilder): ReturnType<InteractionResponseBehavior,Message>? {
            return sendRespond(responseType) { embeds = mutableListOf(embed) }
        }
        

        suspend fun ContextType.sendRespond(responseType: ResponseType? = ResponseType.NORMAL, block: MessageBuilder.()->Unit): ReturnType<InteractionResponseBehavior,Message>? {
            when(this){
                is ContextType.Message -> {
                    when(responseType){
                        ResponseType.NORMAL     -> return this.value.message.sendRespond(block)
                        ResponseType.NO_REPLY   -> return ReturnType.Message(this.value.message.channel.createMessage{ block() })
                        else                    -> return this.value.message.sendRespond(block)
                    }
                }
                is ContextType.Interaction -> {
                    when(responseType){
                        ResponseType.NORMAL     -> return ReturnType.Interaction(this.value.interaction.respondPublic(block))
                        ResponseType.EPHEMERAL  -> return ReturnType.Interaction(this.value.interaction.respondEphemeral(block))
                        ResponseType.NO_REPLY   -> return ReturnType.Message(this.value.interaction.channel.createMessage { block() })
                        else                    -> return ReturnType.Interaction(this.value.interaction.respondPublic(block))
                    }
                }
            }
            return null
        }

        suspend fun ApplicationCommandInteractionCreateEvent.createDefer() = createDefer(responseType = ResponseType.NORMAL)

        suspend fun ApplicationCommandInteractionCreateEvent.createDefer(responseType:ResponseType? = ResponseType.NORMAL): DeferredMessageInteractionResponseBehavior? {
            when(responseType){
                ResponseType.NORMAL             -> return this.interaction.deferPublicResponse()
                ResponseType.EPHEMERAL          -> return this.interaction.deferEphemeralResponse()
                else                            -> {}
            }
            return null

        }

        suspend fun ContextType.createDefer(responseType:ResponseType? = ResponseType.NORMAL): DeferredMessageInteractionResponseBehavior? {
            when(this){
                is ContextType.Message -> {}
                is ContextType.Interaction -> {
                    when(responseType){
                        ResponseType.NORMAL     -> return this.value.interaction.deferPublicResponse()
                        ResponseType.EPHEMERAL  -> return this.value.interaction.deferEphemeralResponse()
                        else                    -> {}
                    }
                }
            }

            return null
        }

    }

}