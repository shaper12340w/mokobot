package dev.shaper.rypolixy.config

object Register {
    suspend fun register(client: Client) {

        client.commandManager.collectCommands(
            listOf(
                dev.shaper.rypolixy.command.commands.interaction.Info(client),
                dev.shaper.rypolixy.command.commands.text.Info(client),
                dev.shaper.rypolixy.command.commands.mutual.Ping(client),
                dev.shaper.rypolixy.command.commands.mutual.Join(client),
                dev.shaper.rypolixy.command.commands.mutual.Leave(client),
                dev.shaper.rypolixy.command.commands.mutual.Play(client),
                dev.shaper.rypolixy.command.commands.mutual.Skip(client),
                dev.shaper.rypolixy.command.commands.mutual.Pause(client),
                dev.shaper.rypolixy.command.commands.mutual.Resume(client),
                dev.shaper.rypolixy.command.commands.mutual.Repeat(client),
                dev.shaper.rypolixy.command.commands.mutual.Playing(client),
                dev.shaper.rypolixy.command.commands.mutual.Shuffle(client),
                dev.shaper.rypolixy.command.commands.mutual.Relate(client),
                dev.shaper.rypolixy.command.commands.mutual.Volume(client),
            )
        )

        client.commandManager.registerInteractionCommand()
    }
}