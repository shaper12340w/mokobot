package dev.shaper.rypolixy.utils.discord.actionrow

import dev.kord.core.entity.interaction.SelectMenuInteraction

interface SelectMenu {
    val length      : ClosedRange<Int>
    val disabled    : Boolean?
    val placeHolder : String?
    val isOnce      : Boolean
    val type        : SelectMenuType
    val options     : MutableList<*>

    suspend fun execute(
        interaction: SelectMenuInteraction,
        kill: () -> Unit
    )
}