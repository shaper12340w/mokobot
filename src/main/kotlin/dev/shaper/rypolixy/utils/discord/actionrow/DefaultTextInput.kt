package dev.shaper.rypolixy.utils.discord.actionrow

import dev.kord.common.entity.TextInputStyle

open class DefaultTextInput(
    override val label      : String,
    override val style      : TextInputStyle,
    override val length     : ClosedRange<Int>? = null,
    override val placeHolder: String?           = null,
    override val required   : Boolean?          = null,
    override val disabled   : Boolean?          = null,
    override val value      : String?           = null
): TextInput