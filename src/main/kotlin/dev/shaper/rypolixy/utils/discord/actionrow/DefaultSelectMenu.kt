package dev.shaper.rypolixy.utils.discord.actionrow

import dev.kord.core.entity.interaction.SelectMenuInteraction

open class DefaultSelectMenu(
    override val length: ClosedRange<Int> = IntRange(1,1),
    override val isOnce: Boolean = false,
    override val type: SelectMenuType,
    override val options: MutableList<*>,
    override val disabled: Boolean? = false,
    override val placeHolder: String? = null,
    val executeFunction: suspend (interaction: SelectMenuInteraction, kill: () -> Unit) -> Unit
): SelectMenu {
    override suspend fun execute(interaction: SelectMenuInteraction, kill: () -> Unit) {
        executeFunction(interaction, kill)
    }
}