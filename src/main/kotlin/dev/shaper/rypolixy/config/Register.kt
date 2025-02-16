package dev.shaper.rypolixy.config

import dev.shaper.rypolixy.command.commands.mutual.player.*

object Register {
    suspend fun register(client: Client) {

        client.commandManager.collectCommands(
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


        client.commandManager.registerInteractionCommand()
    }
}