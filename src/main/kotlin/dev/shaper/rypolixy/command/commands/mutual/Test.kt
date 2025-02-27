package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.context.ResponseType
import dev.shaper.rypolixy.utils.discord.actionrow.*
import java.util.*

class Test(private val client: Client): MutualCommand {

    override val name           : String                    = "test"
    override val description    : String                    = "test"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {

        suspend fun selectMenuExecute(interaction: SelectMenuInteraction, kill: () -> Unit) {
            interaction.respondPublic { content = interaction.values.firstOrNull() ?: "응애" }
        }

        suspend fun buttonExecute(
            interaction: ButtonInteraction,
            edit: (List<Button>) -> ActionRowBuilder,
            kill: () -> Unit
        ) {
            ActionRowManager.CreateModal(
                UUID.randomUUID(),
                ModalData(
                    "와",listOf(DefaultTextInput(
                        label = "센즈",
                        style = TextInputStyle.from(1),
                    ))
                )
            ).apply {
                suspend fun execute(interaction: ModalSubmitInteraction) {
                    val values = interaction.textInputs.entries.toList().firstOrNull()
                    interaction.respondPublic { content = values?.value?.value + " | " + values?.key }
                }
                show(interaction,::execute)
                interaction.channel.createMessage(interaction.componentId)
            }

        }

        val button  = ActionRowManager.CreateButton(UUID.randomUUID()).apply {
            addButton(DefaultButton(
                label = "OK",
                style = ButtonStyle.Primary,
                executeFunction = ::buttonExecute,
            ))
        }.build()
        
        val selectMenu = ActionRowManager.CreateSelectMenu(
            UUID.randomUUID(),
            SelectMenuData(
                listOf(DefaultSelectMenu(
                    type = SelectMenuType.STRING,
                    options = mutableListOf<SelectOptionBuilder>().apply {
                        add(SelectOptionBuilder("와","센즈"))
                    },
                    executeFunction = ::selectMenuExecute
                ))
            )
        ).build()
        
        
        context.sendRespond(
            ResponseType.EPHEMERAL
        ){
            embeds      = mutableListOf(EmbedFrame.info("테스트", "아시는구나"))
            components  = mutableListOf(button,selectMenu)
        }
    }

}