package dev.shaper.rypolixy.utils.discord.actionrow

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji

interface Button {
    var disabled: Boolean?
    var emoji: DiscordPartialEmoji?
    var label: String?
    var style: ButtonStyle?
    var url: String?
}