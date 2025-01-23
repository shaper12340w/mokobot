package dev.shaper.rypolixy.utils.discord

import dev.kord.rest.builder.message.EmbedBuilder
import dev.shaper.rypolixy.utils.discord.TextDesign.Embed.description
import dev.shaper.rypolixy.utils.discord.TextDesign.Embed.title
import dev.shaper.rypolixy.utils.musicplayer.AudioTrack

object EmbedFrame {

    fun info(title:String = "Info",description:String?,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.INFO, title)
            description(description)
            color = Colors.BLURLPLE
        }.apply(builder)
    }

    fun success(title:String = "Success",description:String?,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.SUCCESS, title)
            description(description)
            color = Colors.GREEN
        }.apply(builder)
    }

    fun error(title:String = "Error",description:String?,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.ERROR, title)
            description(description)
            color = Colors.RED
        }.apply(builder)
    }

    fun warning(title:String = "Warning",description:String?,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.WARN, title)
            description(description)
            color = Colors.YELLOW
        }.apply(builder)
    }

    fun loading(title:String = "Loading",description:String?,builder:EmbedBuilder.() -> Unit): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.LOADING, title)
            description(description)
            color = Colors.DARKGREY
        }.apply(builder)

    }

    fun musicInfo(track: AudioTrack,isRecommend:Boolean):EmbedBuilder {
        val info = track.audioTrack.info
        return EmbedBuilder().apply {
            title = "${if(isRecommend) "✅"  else "🎶"} | ${info.title}"
            color = if(isRecommend) Colors.GREEN else Colors.BLURLPLE
            fields = mutableListOf(
                EmbedBuilder.Field().apply {
                    name    = "재생시간"
                    value   = info.length.toString()
                    inline  = true
                },
                EmbedBuilder.Field().apply {
                    name    = "채널"
                    value   = info.author
                    inline  = true
                },
                EmbedBuilder.Field().apply {
                    name    = "링크"
                    value   = "[링크](${info.uri})"
                    inline  = true
                }
            )
            thumbnail = EmbedBuilder.Thumbnail().apply {
                url = info.uri
            }
            footer = EmbedBuilder.Footer().apply {
                text = if(isRecommend)"추천 기능으로 자동 추가됨" else ""
                icon = ""
            }
        }
    }

}