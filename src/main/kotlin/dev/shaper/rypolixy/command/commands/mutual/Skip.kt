package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.rest.builder.message.EmbedBuilder
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.channel
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.getMember
import dev.shaper.rypolixy.utils.discord.Colors
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType
import dev.shaper.rypolixy.utils.musicplayer.MediaUtils


class Skip(private val client: Client): MutualCommand {

    override val name: String
        get() = "skip"

    override val description: String
        get() = "skip to next track"

    override val commandType: TextCommand.CommandType
        get() = TextCommand.CommandType(prefix = null, suffix = null, equals = null)

    override val enabled: Boolean
        get() = true

    override val isInteractive: Boolean
        get() = true

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        //context.sendRespond(ResponseType.NORMAL,EmbedFrame.error("text",null))

    }
}