package dev.shaper.rypolixy.utils.discord.embed

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.MessageBuilder
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.actionrow.ActionRowManager
import dev.shaper.rypolixy.utils.discord.actionrow.Button
import dev.shaper.rypolixy.utils.discord.actionrow.DefaultButton
import dev.shaper.rypolixy.utils.discord.context.DefaultMessageBuilder
import dev.shaper.rypolixy.utils.structure.eventemitter.EventListener
import java.util.UUID

class PageEmbed(
    private val title   :String,
    private val id      :UUID = UUID.randomUUID()
) {

    enum class ButtonStatus {
        PREV, NEXT, NOTHING, ALWAYS
    }

    private var pageIndex = 0
    private var embedMessage: Message? = null
    private var embedButton: MutableList<EventListener<ActionRowManager.ButtonEvent>> = mutableListOf()
    private val embedList = mutableListOf<EmbedBuilder>()

    private val PREV_ID     = "$id#0"
    private val COUNT_ID    = "$id#1"
    private val NEXT_ID     = "$id#2"

    private fun prev(){
        if (pageIndex == 0) return
        pageIndex--
    }

    private fun next(){
        if (pageIndex + 1 == embedList.size) return
        pageIndex++
    }

    private suspend fun pageHandler(
        interaction : ButtonInteraction,
        edit        : (List<Button>) -> ActionRowBuilder,
        kill        : () -> Unit
    ){
        if (embedMessage == null) throw Exception("Base Message not found")
        when(interaction.componentId){
            PREV_ID -> prev()
            NEXT_ID -> next()
            else    -> Unit
        }
        val arbiter = when {
            pageIndex + 1 == embedList.size -> ButtonStatus.NEXT
            pageIndex     == 0              -> ButtonStatus.PREV
            else                            -> ButtonStatus.NOTHING
        }
        interaction.deferPublicMessageUpdate()
        embedButton.forEach { button -> ActionRowManager.emitter.off(button) }
        val editMessage = pageBuilder(embedList[pageIndex],arbiter)
        embedMessage!!.edit { embeds = editMessage.embeds; components = editMessage.components }
    }

    private fun pageBuilder(embed: EmbedBuilder,disableButton:ButtonStatus) : MessageBuilder {
        val buttons = ActionRowManager.CreateButton(id)
            .apply {
                addButton(DefaultButton(
                    label = "<",
                    style = ButtonStyle.Primary,
                    disabled =  disableButton == ButtonStatus.PREV ||
                                disableButton == ButtonStatus.ALWAYS,
                    executeFunction = ::pageHandler
                ))
                addButton(DefaultButton(
                    label = "${pageIndex + 1}/${embedList.size}",
                    style = ButtonStyle.Primary,
                    disabled = true,
                    executeFunction = ::pageHandler
                ))
                addButton(DefaultButton(
                    label = ">",
                    style = ButtonStyle.Primary,
                    disabled =  disableButton == ButtonStatus.NEXT ||
                                disableButton == ButtonStatus.ALWAYS,
                    executeFunction = ::pageHandler
                ))
            }
        val build   = buttons.build()
        embedButton.addAll(buttons.getListeners())
        return DefaultMessageBuilder(
            components  = mutableListOf(build),
            embeds      = mutableListOf(embed)
        )
    }

    fun addEmbed(builder: EmbedBuilder): PageEmbed {
        embedList.add(builder)
        return this
    }

    fun autoSplitEmbed(text:String,count:Int = 15): PageEmbed {
        val splitString = text.split("\n").chunked(count)
        splitString.forEachIndexed { index,chunkedString ->
            val embed = EmbedFrame.list(
                title   = title,
                des     = chunkedString.joinToString("\n"),
                count   = index*count,
            )
            embedList.add(embed)
        }
        return this
    }

    fun build(): MessageBuilder {
        if (embedList.isEmpty()) throw Exception("EmbedList is empty")
        val status = if(embedList.size == 1) ButtonStatus.ALWAYS else ButtonStatus.PREV
        return pageBuilder(embedList[0],status)
    }

    fun setMessage(message : Message){
        embedMessage = message
    }
}