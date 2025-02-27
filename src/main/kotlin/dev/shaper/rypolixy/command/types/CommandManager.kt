package dev.shaper.rypolixy.command.types

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.ChatInputCommandBehavior
import dev.shaper.rypolixy.command.types.TextCommand.ResponseData
import dev.shaper.rypolixy.config.Client
import dev.kord.core.event.message.MessageCreateEvent
import dev.shaper.rypolixy.config.Configs
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.io.database.Database
import dev.shaper.rypolixy.utils.io.database.DatabaseManager
import kotlinx.coroutines.flow.onEach
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
                val options = (if(command.isBlank()) args.drop(1) else args.drop(2))
                    .map {
                        it.removeSurrounding("\"")
                            .replace(Regex("""\s*(?<==)\s*"|^"|"$"""), "")
                    }
                val parser = Parser()
                logger.debug { "keyword: $keyword | command: $command | options:$options" }
                if (textCommand[keyword] != null){
                    parser.apply {
                        setName(textCommand[keyword]!!.name)
                        setApplicationDescription(textCommand[keyword]!!.description)
                    }
                    textCommand[keyword]?.setup(parser)
                    textCommand[keyword]?.execute(event, ResponseData(command, parser.parse(options.toTypedArray())))
                    logger.info { "[Message][ KEY :$keyword ] (guildId : ${event.guildId} / channelId : ${event.message.channelId})" }
                }

                if (mutualCommand[keyword] != null){
                    parser.apply {
                        setName(mutualCommand[keyword]!!.name)
                        setApplicationDescription(mutualCommand[keyword]!!.description)
                    }
                    mutualCommand[keyword]?.setup(parser)
                    mutualCommand[keyword]?.execute(ContextType.Message(event), ResponseData(command, parser.parse(options.toTypedArray())))
                    logger.info { "[Message][ KEY : $keyword ] (guildId : ${event.guildId} / channelId : ${event.message.channelId})" }
                }

            }

        }
    }

    suspend fun registerInteractionCommand() : MutableMap<CommandStructure, ChatInputCommandBehavior> {
        //client.kord.rest.interaction.getGlobalApplicationCommands(client.kord.selfId).forEach { command -> client.kord.rest.interaction.deleteGlobalApplicationCommand(client.kord.selfId,command.id) }
        val commands = mutableMapOf<CommandStructure,ChatInputCommandBehavior>()
        suspend fun registerGlobal(command: CommandStructure)
            = commands.put(command,client.kord.createGlobalChatInputCommand(command.name,command.description){ (command as InteractionCommand).setup(this) })
        suspend fun registerGuild(command: CommandStructure,guildId: Snowflake)
            = commands.put(command,client.kord.createGuildChatInputCommand(guildId,command.name,command.description){ (command as InteractionCommand).setup(this) })

        val interactionCommands = interactionCommand.filterValues{ it.enabled ?: false }
        val mutualCommands      = mutualCommand     .filterValues{ it.enabled ?: false }.filterValues { it.isInteractive }
        when(Configs.SETTINGS.register){
            "GLOBAL" -> {
                mutualCommands.forEach      { registerGlobal(it.value) }
                interactionCommands.forEach { registerGlobal(it.value) }
            }
            "SERVER" -> {
                //val commandUUIDs = Database.
                client.kord.guilds.onEach { guild ->
                    val guildData = DatabaseManager.getGuildData(guild.id)
                    mutualCommands.forEach { command ->
                        if(commands[command as CommandStructure] !=null){
//                            if(guildData.guildData.excludedCommands)
                            registerGuild(command,guild.id)

                        }
                    }
                }
            }
            "NONE"   -> Unit
            else     -> throw Exception("Unknown Register Property")
        }
        return commands
    }

    fun collectCommands(commands:Iterable<CommandStructure>) : MutableMap<CommandStructure, String> {
        val commandCollector = mutableMapOf<CommandStructure, String>()
        commands.forEach{ command ->
            val pkgName = command::class.java.`package`?.name?.substringAfterLast("commands.")
            when(command) {
                is MutualCommand        -> mutualCommand       [command.name] = command
                is TextCommand          -> textCommand         [command.name] = command
                is MessageCommand       -> messageCommand      [command.name] = command
                is InteractionCommand   -> interactionCommand  [command.name] = command
            }
            commandCollector[command] = pkgName!!
        }
        return commandCollector
    }

    fun databaseRegister(
        commandId : Snowflake,
        commandStruct: CommandStructure,
        pkg:String
    ){
        Database.initCommand(commandStruct,commandId,pkg)
    }

}





