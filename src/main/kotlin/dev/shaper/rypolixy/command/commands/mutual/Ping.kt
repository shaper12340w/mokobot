package dev.shaper.rypolixy.command.commands.mutual

import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType

class Ping(private val client: Client): MutualCommand {

    override val name           : String                    = "ping"
    override val description    : String                    = "Pong!"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        context.sendRespond(
            ResponseType.EPHEMERAL,
            EmbedFrame.info("Ping", "pong")
        )
    }

}