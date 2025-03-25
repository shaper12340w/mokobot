package dev.shaper.rypolixy.utils.discord.context

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.*
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.response.EphemeralMessageInteractionResponse
import dev.kord.core.entity.interaction.response.MessageInteractionResponse
import dev.kord.core.entity.interaction.response.PublicMessageInteractionResponse
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.MessageBuilder
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ResponseManager{

    companion object{


        suspend fun InteractionResponseBehavior.sendRespond(embed: EmbedBuilder): ReturnType? = sendRespond(responseType = ResponseType.NORMAL,embed = embed)

        suspend fun InteractionResponseBehavior.sendRespond(responseType: ResponseType?, embed: EmbedBuilder): ReturnType?{
            return sendRespond(responseType) { embeds = mutableListOf(embed) }
        }

        suspend fun InteractionResponseBehavior.sendRespond(block: MessageBuilder.()->Unit): ReturnType? = sendRespond(responseType = ResponseType.NORMAL){ block() }

        suspend fun InteractionResponseBehavior.sendRespond(responseType: ResponseType?, block: MessageBuilder.()->Unit): ReturnType? {
            when(this){
                is MessageInteractionResponseBehavior           -> return ReturnType.Interaction(this.edit(block))
                is DeferredMessageInteractionResponseBehavior   -> {
                    if(this is ChatInputCommandInteractionCreateEvent){
                        return when(responseType) {
                            ResponseType.NORMAL -> ReturnType.Interaction(this.respond(block))
                            ResponseType.EPHEMERAL -> ReturnType.Interaction(this.interaction.respondEphemeral(block))
                            ResponseType.NO_REPLY -> ReturnType.Message(this.interaction.channel.createMessage(block))
                            else -> ReturnType.Interaction(this.respond(block))
                        }
                    }
                    return ReturnType.Interaction(this.respond(block))
                }
                is FollowupPermittingInteractionResponseBehavior -> {  }
                else -> {  }
            }
            return null
        }

        suspend fun MessageBehavior.sendRespond(embed: EmbedBuilder): ReturnType = sendRespond(responseType = ResponseType.NORMAL,embed = embed)

        suspend fun MessageBehavior.sendRespond(responseType: ResponseType?, embed: EmbedBuilder): ReturnType {
            return ReturnType.Message(this.reply { embeds = mutableListOf(embed) })
        }

        suspend fun MessageBehavior.sendRespond(block: MessageBuilder.()->Unit): ReturnType {
            return ReturnType.Message(this.reply { block() })
        }

        suspend fun ContextType.sendRespond(embed: EmbedBuilder): ReturnType? = sendRespond(responseType = ResponseType.NORMAL,embed = embed)

        suspend fun ContextType.sendRespond(responseType: ResponseType?, embed: EmbedBuilder): ReturnType? {
            return sendRespond(responseType) { embeds = mutableListOf(embed) }
        }
        

        suspend fun ContextType.sendRespond(responseType: ResponseType? = ResponseType.NORMAL, block: MessageBuilder.()->Unit): ReturnType? {
            when(this){
                is ContextType.Message -> {
                    return when(responseType){
                        ResponseType.NORMAL -> this.value.message.sendRespond(block)
                        ResponseType.NO_REPLY -> ReturnType.Message(this.value.message.channel.createMessage { block() })
                        else                    -> this.value.message.sendRespond(block)
                    }
                }
                is ContextType.Interaction -> {
                    return when(responseType){
                        ResponseType.NORMAL -> ReturnType.Interaction(this.value.interaction.respondPublic(block))
                        ResponseType.EPHEMERAL -> ReturnType.Interaction(this.value.interaction.respondEphemeral(block))
                        ResponseType.NO_REPLY -> ReturnType.Message(this.value.interaction.channel.createMessage { block() })
                        else                    -> ReturnType.Interaction(this.value.interaction.respondPublic(block))
                    }
                }
            }
        }

        suspend fun ApplicationCommandInteractionCreateEvent.createDefer() = createDefer(responseType = ResponseType.NORMAL)

        suspend fun ApplicationCommandInteractionCreateEvent.createDefer(responseType: ResponseType? = ResponseType.NORMAL): DeferredMessageInteractionResponseBehavior? {
            when(responseType){
                ResponseType.NORMAL     -> return this.interaction.deferPublicResponse()
                ResponseType.EPHEMERAL  -> return this.interaction.deferEphemeralResponse()
                else                            -> {}
            }
            return null

        }

        suspend fun ContextType.createDefer(responseType: ResponseType? = ResponseType.NORMAL): DeferResponse {
            when(this){
                is ContextType.Message      -> {
                    val response = this.value.message.channel.createMessage {
                        embeds = mutableListOf(EmbedFrame.loading())
                    }
                    return DeferResponse.Message(response)
                }
                is ContextType.Interaction  -> {
                    val response = when(responseType){
                        ResponseType.NORMAL     -> this.value.interaction.deferPublicResponse()
                        ResponseType.EPHEMERAL  -> this.value.interaction.deferEphemeralResponse()
                        else                    -> this.value.interaction.deferPublicResponse()
                    }
                    return DeferResponse.Interaction(response)
                }
            }
        }

        suspend fun DeferResponse.deferReply(builder: MessageBuilder.()->Unit): ReturnType {
            return when(this) {
                is DeferResponse.Message        -> ReturnType.Message(this.res.edit { builder() })
                is DeferResponse.Interaction    -> ReturnType.Interaction(this.res.respond { builder() })
            }
        }

        fun ReturnType.getMessage() : Message? {
            return when(this){
                is ReturnType.Message -> this.data
                is ReturnType.Interaction -> when(this.data){
                    is EphemeralMessageInteractionResponse  -> this.data.message
                    is PublicMessageInteractionResponse     -> this.data.message
                    is MessageInteractionResponse           -> this.data.message
                    else -> null
                }
            }
        }

        suspend fun ReturnType.delete() {
            when(this){
                is ReturnType.Message       -> this.data.delete()
                is ReturnType.Interaction   -> this.data.delete()
            }
        }

        fun ReturnType.deleteAfter(time: Int){
            var timerTime = time
            val timer = kotlin.concurrent.timer(period = 1000L) {
                timerTime -= 1000
                if(timerTime <= 0){
                    (this@timer).cancel()
                    CoroutineScope(Dispatchers.Default).launch {
                        when(this@deleteAfter) {
                            is ReturnType.Message -> this@deleteAfter.data.asMessage().delete()
                            is ReturnType.Interaction -> {
                                when(val data = this@deleteAfter.data) {
                                    is PublicMessageInteractionResponse                     -> data.delete()
                                    is EphemeralMessageInteractionResponse                  -> data.delete()
                                    else -> Unit
                                }
                            }
                        }
                    }

                }
            }
        }

    }

}