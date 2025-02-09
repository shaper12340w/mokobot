package dev.shaper.rypolixy.command.commands.mutual.player

import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType


class Relate(private val client: Client): MutualCommand {

    override val name           : String                    = "relate"
    override val description    : String                    = "Get related track when play ended"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        val session = client.lavaClient.sessions[context.guildId]
        if (session == null) {
            context.sendRespond(
                ResponseType.NORMAL,
                EmbedFrame.error("재생 중이 아닙니다",null)
            )
        }
        else {
            context.sendRespond(
                ResponseType.NORMAL,
                EmbedFrame.info(if(client.lavaClient.related(context.guildId)!!) "트랙 추천 활성화됨" else "트랙 추천 비활성화됨",null) {
                    footer { text = "[!] 현재 Youtube 와 Soundcloud 소스만 지원됩니다" }
                }
            )
        }
    }
}