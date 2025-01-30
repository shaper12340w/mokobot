package dev.shaper.rypolixy.command.types

import dev.shaper.rypolixy.command.types.TextCommand.ResponseData
import dev.shaper.rypolixy.config.Client
import dev.kord.core.event.message.MessageCreateEvent
import dev.shaper.rypolixy.logger
import us.jimschubert.kopper.Parser

class CommandManager(private val client: Client) {
    val textCommand         = HashMap<String, TextCommand>        ()
    val mutualCommand       = HashMap<String, MutualCommand>      ()
    val messageCommand      = HashMap<String, MessageCommand>     ()
    val interactionCommand  = HashMap<String, InteractionCommand> ()

    suspend fun executeTextCommand(event:MessageCreateEvent) {
        val input = event.message.content
        if (input.startsWith(TextCommand.commonPrefix)) {
            val regex       = """"([^"]*)"|(\S+)""".toRegex()
            val args        = regex.findAll(input.split(TextCommand.commonPrefix)[1]).map { it.value }.toList()

            if (args.isNotEmpty()) {
                val keyword = args[0]
                val command = if(args.size > 1 && !args[1].startsWith("-")) args[1] else ""
                val options = if(command.isBlank()) args.drop(1) else args.drop(2)
                val parser = Parser()
                logger.debug { "keyword: $keyword | command: $command | options:$options" }
                if (textCommand[keyword] != null){
                    parser.apply {
                        setName(textCommand[keyword]!!.name)
                        setApplicationDescription(textCommand[keyword]!!.description)
                    }
                    textCommand[keyword]?.setup(parser)
                    textCommand[keyword]?.execute(event, ResponseData(command, parser.parse(options.toTypedArray())))
                }

                if (mutualCommand[keyword] != null){
                    parser.apply {
                        setName(mutualCommand[keyword]!!.name)
                        setApplicationDescription(mutualCommand[keyword]!!.description)
                    }
                    mutualCommand[keyword]?.setup(parser)
                    mutualCommand[keyword]?.execute(ContextType.Message(event), ResponseData(command, parser.parse(options.toTypedArray())))
                }

            }

        }
    }


    suspend fun registerInteractionCommand(){
        interactionCommand.filterValues{ it.enabled ?: false }.forEach { (key,command) ->
            client.kord.createGlobalChatInputCommand(command.name,command.description){
                command.setup(this)
            }
        }

        mutualCommand.filterValues{ it.enabled ?: false }.filterValues { it.isInteractive } .forEach { (key,command) ->
            client.kord.createGlobalChatInputCommand(command.name,command.description){
                command.setup(this)
            }
        }

    }

    fun collectCommands(commands:Iterable<CommandStructure>){
        commands.forEach{ command ->
            when(command) {
                is MutualCommand        -> mutualCommand       [command.name] = command
                is TextCommand          -> textCommand         [command.name] = command
                is MessageCommand       -> messageCommand      [command.name] = command
                is InteractionCommand   -> interactionCommand  [command.name] = command
            }
        }
    }

}





