package dev.shaper.rypolixy.command

import dev.shaper.rypolixy.command.interaction.InteractionCommand
import dev.shaper.rypolixy.command.message.MessageCommand
import dev.shaper.rypolixy.command.text.TextCommand
import dev.shaper.rypolixy.command.text.TextCommand.ResponseData
import dev.shaper.rypolixy.config.Client

import dev.kord.core.event.message.MessageCreateEvent

class CommandManager(private val client: Client) {
    private val textCommand         :MutableList<TextCommand>        = mutableListOf()
    private val messageCommand      :MutableList<MessageCommand>     = mutableListOf()
    private val interactionCommand  :MutableList<InteractionCommand> = mutableListOf()

    suspend fun executeTextCommand(event:MessageCreateEvent){
        val input  = event.message.content
        for(value in textCommand){

            when {
                value.commandType.equals != null -> {
                    value.commandType.equals?.find { it == input }
                }
                value.commandType.prefix != null -> {
                    value.commandType.prefix?.find { input.startsWith(it) }
                }
                value.commandType.suffix != null -> {
                    value.commandType.suffix?.find { input.endsWith(it) }
                }
                else -> {
                    if(input.startsWith(value.commonPrefix)){

                        val regex       = Regex("^${value.commonPrefix}(\\w+)\\s*(.*)")
                        val matchResult = regex.find(input)

                        if (matchResult != null) {
                            val keyword        = matchResult.groupValues[1]
                            val remainingValue = matchResult.groupValues[2]

                            if(keyword == value.name)
                                value.execute(event, ResponseData(keyword,remainingValue))
                        }
                    }
                }
            }

        }
    }

    suspend fun registerMessageCommand(){
        //client.kord.createGuildApplicationCommands()
    }

    fun collectCommands(commands:Iterable<CommandStructure>){
        commands.forEach{ command ->
            when(command) {
                is TextCommand        -> textCommand       .add(command)
                is MessageCommand     -> messageCommand    .add(command)
                is InteractionCommand -> interactionCommand.add(command)
            }
        }
    }

}





