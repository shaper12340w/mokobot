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
        val runtime = Runtime.getRuntime()

        val totalMemory = runtime.totalMemory()     // JVM이 확보한 총 메모리 (Heap)
        val freeMemory = runtime.freeMemory()       // 사용 가능한 메모리
        val usedMemory = totalMemory - freeMemory   // 사용 중인 메모리
        val maxMemory = runtime.maxMemory()         // JVM이 사용할 수 있는 최대 메모리

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
                                "Uptime : ${uptime.seconds.inWholeDays}d ${uptime.seconds.inWholeHours % 24}h ${uptime.seconds.inWholeMinutes % 60}m ${uptime.seconds.inWholeSeconds% 60}s\n" +
                                "Memory : \n" +
                                " - Total : ${totalMemory / (1024 * 1024)} MB\n" +
                                " - Free  : ${freeMemory / (1024 * 1024)} MB\n" +
                                " - Used  : ${usedMemory / (1024 * 1024)} MB\n" +
                                " - Max   : ${maxMemory / (1024 * 1024)} MB```"
                }
            )
        }
    }


}