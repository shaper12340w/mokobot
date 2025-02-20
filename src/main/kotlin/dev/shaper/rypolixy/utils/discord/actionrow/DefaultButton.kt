package dev.shaper.rypolixy.utils.discord.actionrow

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.rest.builder.component.ActionRowBuilder

open class DefaultButton(
    override var disabled   : Boolean?              = null,
    override var emoji      : DiscordPartialEmoji?  = null,
    override var label      : String?               = null,
    override var style      : ButtonStyle?          = null,
    override var url        : String?               = null,
    val executeFunction     : suspend (
        interaction: ButtonInteraction,
        edit: (List<Button>) -> ActionRowBuilder,
        kill: () -> Unit
    ) -> Unit
):ButtonInit {
    override suspend fun execute(
        interaction : ButtonInteraction,
        edit        : (List<Button>) -> ActionRowBuilder,
        kill        : () -> Unit
    ) {
        executeFunction(interaction, edit, kill)
    }
}