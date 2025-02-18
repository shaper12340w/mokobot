package dev.shaper.rypolixy.command.commands.mutual.player

import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType

class Skip(private val client: Client): MutualCommand {

    override val name           : String                    = "skip"
    override val description    : String                    = "Skip to next track"
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
            //TODO : Add option to skip Playlist
            val track = client.lavaClient.next(context.guildId)
            context.sendRespond(
                ResponseType.NORMAL,
                EmbedFrame.info("다음 곡을 재생합니다",null) {
                    footer { text = track?.title ?: "" }
                }
            )
        }
    }
}