package dev.shaper.rypolixy.command.commands.interaction

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.shaper.rypolixy.command.types.InteractionCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.CheckNetwork
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.createDefer
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.context.ResponseType
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.seconds

class Info(private val client: Client): InteractionCommand {

    override val name           : String    = "info"
    override val description    : String    = "Get system info"
    override val enabled        : Boolean   = true

    override suspend fun execute(context: ChatInputCommandInteractionCreateEvent) {
        val pingResult = CheckNetwork.pingURL("https://www.discord.com") ?: "Error"
        val uptime = ManagementFactory.getRuntimeMXBean().uptime / 1000
        val response = context.createDefer(ResponseType.EPHEMERAL)!!

        client.logger.debug { "Called Info" }
        response.sendRespond {
            embeds = mutableListOf(
                EmbedBuilder().apply {
                    title = "Info"
                    description =
                        "```Ping : $pingResult ms\n" +
                                "JVM version : ${System.getProperty("java.vm.version")}\n" +
                                "OS Name : ${System.getProperty("os.name")}\n" +
                                "Arch : ${System.getProperty("os.arch")}\n" +
                                "Uptime : ${uptime.seconds.inWholeDays}d ${uptime.seconds.inWholeHours % 24}h ${uptime.seconds.inWholeMinutes % 60}m ${uptime.seconds.inWholeSeconds% 60}s```"
                }
            )
        }
    }


}