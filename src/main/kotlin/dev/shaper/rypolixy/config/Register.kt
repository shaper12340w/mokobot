package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.command.commands.interaction.Info

object Register {
    suspend fun register(client: Client) {

        client.commandManager.collectCommands(
            listOf(
                Info(client),
                dev.shaper.rypolixy.command.commands.text.Info(client)
            )
        )

        client.commandManager.registerInteractionCommand()
    }
}