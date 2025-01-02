package dev.shaper.rypolixy.config

object Register {
    suspend fun register(client: Client) {

        client.commandManager.collectCommands(
            listOf(
                dev.shaper.rypolixy.command.commands.interaction.Info(client),
                dev.shaper.rypolixy.command.commands.text.Info(client),
                dev.shaper.rypolixy.command.commands.mutual.Ping(client),
            )
        )

        client.commandManager.registerInteractionCommand()
    }
}