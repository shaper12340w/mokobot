package dev.shaper.rypolixy.command.commands.mutual

import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.CommandCaller

class Resume(private val client: Client): MutualCommand {

    override val name           : String                    = "resume"
    override val description    : String                    = "Resume playing track"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?)
       = CommandCaller.call(client,"pause",context)

}