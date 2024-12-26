package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType

class Ping(private val client: Client): MutualCommand {

    override val name       : String
        get()          = "ping"

    override val description: String
        get()          = "pong"

    override val commandType: TextCommand.CommandType
        get()          = TextCommand.CommandType(prefix = null, suffix = null, equals = null)

    override val enabled    : Boolean
        get() = true

    override val isInteractive: Boolean
        get() = true

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        context.sendRespond(ResponseType.EPHEMERAL,EmbedFrame.info("Ping", "pong"))
    }

}