package dev.shaper.rypolixy.command.commands.text

import dev.kord.common.entity.Snowflake
import dev.shaper.rypolixy.config.Client
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.utils.CheckNetwork
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.sendRespond
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.seconds

class Info(private val client: Client) : TextCommand {

    override val name           : String                    = "info"
    override val description    : String                    = "Get system info"
    override val enabled        : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override suspend fun execute(event: MessageCreateEvent, res: TextCommand.ResponseData?) {
        val pingResult = CheckNetwork.pingURL("https://www.discord.com") ?: "Error"
        val uptime = ManagementFactory.getRuntimeMXBean().uptime / 1000
        client.logger.debug { "Called Info" }
        event.message.sendRespond {
            embed {
                title = "Info"
                description =
                    "```Ping : $pingResult ms\n" +
                            "JVM version : ${System.getProperty("java.vm.version")}\n" +
                            "OS Name : ${System.getProperty("os.name")}\n" +
                            "Arch : ${System.getProperty("os.arch")}\n" +
                            "Uptime : ${uptime.seconds.inWholeDays}d ${uptime.seconds.inWholeHours % 24}h ${uptime.seconds.inWholeMinutes % 60}m ${uptime.seconds.inWholeSeconds% 60}s```"
            }
        }

    }

}
