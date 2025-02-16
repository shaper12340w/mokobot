package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.actionRow
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType
import dev.shaper.rypolixy.utils.discord.actionrow.*
import dev.shaper.rypolixy.utils.io.database.Database.logger
import java.util.*

class Test(private val client: Client): MutualCommand {

    override val name           : String                    = "test"
    override val description    : String                    = "test"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {

        data class TextInputs(
            override val label: String,
            override val length: ClosedRange<Int>?,
            override val placeHolder: String?,
            override val required: Boolean?,
            override val disabled: Boolean?,
            override val style: TextInputStyle,
            override val value: String?
        ): TextInput
        
        data class NormalButton(
            override var disabled: Boolean?,
            override var emoji: DiscordPartialEmoji?,
            override var label: String?,
            override var style: ButtonStyle?,
            override var url: String?,
        ) :ButtonInit {
            override suspend fun execute(
                interaction: ButtonInteraction,
                edit: (List<Button>) -> ActionRowBuilder,
                kill: () -> Unit
            ) {
                ActionRowManager.CreateModal(
                    UUID.randomUUID(),
                    ModalData(
                        "와",listOf(TextInputs(
                            label = "센즈",
                            style = TextInputStyle.from(1),
                            length = null,
                            placeHolder = null,
                            required = null,
                            disabled = null,
                            value = null
                        ))
                    )
                ).apply {
                    suspend fun execute(interaction: ModalSubmitInteraction) {
                        val values = interaction.textInputs.entries.toList().firstOrNull()
                        interaction.respondPublic { content = values?.value?.value + " | " + values?.key }
                    }
                    show(interaction,::execute)
                }

            }
        }
        
        

        val button  = ActionRowManager.CreateButton(UUID.randomUUID()).apply {
            addButton(NormalButton(false,null,"OK",ButtonStyle.Primary,null))
        }.build()
        
        context.sendRespond(
            ResponseType.EPHEMERAL
        ){
            embeds      = mutableListOf(EmbedFrame.info("테스트", "아시는구나"))
            components  = mutableListOf(button)
        }
    }

}