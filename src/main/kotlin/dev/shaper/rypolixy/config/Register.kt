package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.command.commands.mutual.player.*
import dev.shaper.rypolixy.command.types.TextCommand

object Register {
    suspend fun register(client: Client) {

        val collectedCommands = client.commandManager.collectCommands(
            listOf(
                dev.shaper.rypolixy.command.commands.interaction.Info(client),
                dev.shaper.rypolixy.command.commands.text.Info(client),
                dev.shaper.rypolixy.command.commands.mutual.Ping(client),
                dev.shaper.rypolixy.command.commands.mutual.Profile(client),
                dev.shaper.rypolixy.command.commands.mutual.Test(client),
                Join    (client),
                Leave   (client),
                Play    (client),
                Skip    (client),
                Pause   (client),
                Resume  (client),
                Repeat  (client),
                Playing (client),
                Shuffle (client),
                Relate  (client),
                Volume  (client),
            )
        )
        val registeredCommands = client.commandManager.registerInteractionCommand()

        collectedCommands.forEach { (structure, pkg) ->
            if(structure !is TextCommand)
                if(registeredCommands.isNotEmpty() && registeredCommands[structure] != null)
                client.commandManager.databaseRegister(registeredCommands[structure]!!.id,structure,pkg)
        }
    }
}