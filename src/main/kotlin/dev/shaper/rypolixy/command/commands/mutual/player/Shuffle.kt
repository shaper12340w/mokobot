package dev.shaper.rypolixy.command.commands.mutual.player

import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.context.ResponseType

class Shuffle(private val client: Client): MutualCommand {

    override val name           : String                    = "shuffle"
    override val description    : String                    = "Shuffle options for playing track"
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
                EmbedFrame.info(if(client.lavaClient.shuffle(context.guildId)) "셔플 활성화됨" else "셔플 비활성화됨",null)
            )
        }
    }
}