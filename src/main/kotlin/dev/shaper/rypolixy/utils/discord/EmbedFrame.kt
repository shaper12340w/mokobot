package dev.shaper.rypolixy.utils.discord

import dev.kord.rest.builder.message.EmbedBuilder
import dev.shaper.rypolixy.utils.discord.TextDesign.Embed.description
import dev.shaper.rypolixy.utils.discord.TextDesign.Embed.title

object EmbedFrame {

    fun info(title:String = "Info",description:String,builder:EmbedBuilder.() -> Unit): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.INFO, title)
            description(description)
            color = Colors.BLURLPLE
        }
    }

    fun success(title:String = "Success",description:String,builder:EmbedBuilder.() -> Unit): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.SUCCESS, title)
            description(description)
            color = Colors.GREEN
        }
    }

    fun error(title:String = "Error",description:String,builder:EmbedBuilder.() -> Unit): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.ERROR, title)
            description(description)
            color = Colors.RED
        }
    }

    fun warning(title:String = "Warning",description:String,builder:EmbedBuilder.() -> Unit): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.WARN, title)
            description(description)
            color = Colors.YELLOW
        }
    }

    fun loading(title:String = "Loading",description:String,builder:EmbedBuilder.() -> Unit): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.LOADING, title)
            description(description)
            color = Colors.DARKGREY
        }
    }
}