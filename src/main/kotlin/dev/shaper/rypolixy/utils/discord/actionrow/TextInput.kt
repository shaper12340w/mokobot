package dev.shaper.rypolixy.utils.discord.actionrow

import dev.kord.common.entity.TextInputStyle

interface TextInput {
    val label: String
    val length: ClosedRange<Int>?
    val placeHolder: String?
    val required: Boolean?
    val disabled: Boolean?
    val style: TextInputStyle
    val value: String?
}