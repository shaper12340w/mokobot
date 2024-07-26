package dev.shaper.rypolixy.command.text

import dev.shaper.rypolixy.config.Client
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import dev.shaper.rypolixy.utils.CheckNetwork
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.seconds

class Info(private val client: Client) : TextCommand() {

    override val name       : String
        get()          = "info"

    override val description: String
        get()          = "get system info"

    override val commandType: CommandType
        get()          = CommandType(prefix = null, suffix = null, equals = null)

    override suspend fun execute(event: MessageCreateEvent, res: ResponseData?) {
        val pingResult = CheckNetwork.pingURL("https://www.discord.com") ?: "Error"
        val uptime = ManagementFactory.getRuntimeMXBean().uptime / 1000
        client.logger.debug { "Called Info" }
        event.message.channel.createEmbed {
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
