package dev.shaper.rypolixy.utils.discord

import dev.kord.core.entity.Member
import dev.kord.rest.builder.message.EmbedBuilder
import dev.shaper.rypolixy.utils.discord.TextDesign.Embed.description
import dev.shaper.rypolixy.utils.discord.TextDesign.Embed.title
import dev.shaper.rypolixy.utils.musicplayer.MediaBehavior
import dev.shaper.rypolixy.utils.musicplayer.MediaTrack
import kotlinx.coroutines.flow.asFlow
import kotlinx.datetime.Clock
import java.util.Date

object EmbedFrame {

    fun info(title:String = "Info",description:String? = null,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Server.INFO, title)
            description(description)
            color = Colors.BLURLPLE
        }.apply(builder)
    }

    fun success(title:String = "Success",description:String? = null,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.SUCCESS, title)
            description(description)
            color = Colors.GREEN
        }.apply(builder)
    }

    fun error(title:String = "Error",description:String? = null,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.ERROR, title)
            description(description)
            color = Colors.RED
        }.apply(builder)
    }

    fun warning(title:String = "Warning",description:String? = null,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Default.WARN, title)
            description(description)
            color = Colors.YELLOW
        }.apply(builder)
    }

    fun loading(title:String = "Loading",description:String? = null,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title(Emoji.Server.LOADING, title)
            description(description)
            color = Colors.BLURLPLE
        }.apply(builder)

    }

    fun musicInfo(track: MediaTrack.Track, avatar: String? = "", isRecommend:Boolean = false):EmbedBuilder {
        return EmbedBuilder().apply {
            title = "${if(isRecommend) "✅"  else "🎶"} | ${track.title}"
            color = if(isRecommend) Colors.GREEN else Colors.BLURLPLE
            fields = mutableListOf(
                EmbedBuilder.Field().apply {
                    name    = "재생시간"
                    value   = track.duration.toString()
                    inline  = true
                },
                EmbedBuilder.Field().apply {
                    name    = "채널"
                    value   = track.author
                    inline  = true
                },
                EmbedBuilder.Field().apply {
                    name    = "링크"
                    value   = "[링크](${track.url?.takeIf { it.length > 500 }?.substring(0, 500) ?: track.url ?: "N/A"})"
                    inline  = true
                }
            )
            timestamp = Clock.System.now()
            thumbnail = EmbedBuilder.Thumbnail().apply {
                url = track.thumbnail?: ""
            }
            footer = EmbedBuilder.Footer().apply {
                text = if(isRecommend)"추천 기능으로 자동 추가됨" else ""
                icon = avatar ?: ""
            }
        }
    }

    fun list(title: String,des:String,builder:EmbedBuilder.() -> Unit = {}): EmbedBuilder {
        return EmbedBuilder().apply {
            title("",title)
            description = des.split("\n").mapIndexed { index, s -> "${index+1}. $s"  }.joinToString("\n")
        }.apply(builder)
    }

}