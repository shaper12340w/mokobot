package dev.shaper.rypolixy.utils.discord

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import us.jimschubert.kopper.ArgumentCollection
import us.jimschubert.kopper.Parser

object CommandCaller {
    suspend fun call(
        client: Client,
        name: String,
        context: Any,
        vararg arguments: String
    ) {
        val manager = client.commandManager
        val buildArg = build(arguments.asList())
        val resData = TextCommand.ResponseData(if(arguments.isNotEmpty()) arguments[0] else "", buildArg)
        when (context) {
            is ChatInputCommandInteractionCreateEvent   -> manager.interactionCommand[name]?.execute(context)
            is MessageCreateEvent                       -> manager.textCommand[name]?.execute(context, resData)
            is ContextType                              -> manager.mutualCommand[name]?.execute(context, resData)
        }
    }

    private fun build(args: List<String>): ArgumentCollection {
        val parser = Parser()
        args.forEach { arg -> parser.flag(arg) }
        return parser.parse(args.toTypedArray())
    }
}
