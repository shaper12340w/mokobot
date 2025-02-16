package dev.shaper.rypolixy.utils.discord.actionrow

import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.rest.builder.component.ActionRowBuilder

interface ButtonInit:Button {
    suspend fun execute(
        interaction: ButtonInteraction,
        edit: (List<Button>) -> ActionRowBuilder,
        kill: () -> Unit
    )
}