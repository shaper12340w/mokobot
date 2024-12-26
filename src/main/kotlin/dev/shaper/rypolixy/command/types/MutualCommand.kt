package dev.shaper.rypolixy.command.types

interface MutualCommand: TextCommand,InteractionCommand {

    suspend fun execute(context: ContextType, res: TextCommand.ResponseData?)

}