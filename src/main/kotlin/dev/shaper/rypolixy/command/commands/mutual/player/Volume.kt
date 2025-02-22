package dev.shaper.rypolixy.command.commands.mutual.player

import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.context.ResponseType
import us.jimschubert.kopper.Parser

class Volume(private val client: Client): MutualCommand {

    override val name           : String                    = "volume"
    override val description    : String                    = "Set player volume"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override fun setup(builder: ChatInputCreateBuilder) {
        builder.apply {
            integer("volume","volume count"){ required = true }
        }
    }

    override fun setup(builder: Parser) {
        builder.apply {
            option("v",listOf("volume"),"volume count")
        }
    }

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        val session = client.lavaClient.sessions[context.guildId]
        if (session == null) {
            context.sendRespond(
                ResponseType.NORMAL,
                EmbedFrame.error("재생 중이 아닙니다",null)
            )
        }
        else {
            when (context){
                is ContextType.Interaction  -> {
                    val volume = context.value.interaction.command.integers["volume"]
                    client.lavaClient.setVolume(context.guildId,volume!!.toInt())
                    context.sendRespond(
                        ResponseType.NORMAL,
                        EmbedFrame.info("음량이 $volume 으로 설정되었습니다",null)
                    )
                }
                is ContextType.Message      -> {
                    val volume = (res?.command ?: res?.options?.option("v"))?.toIntOrNull()
                    if(volume == null || res?.options?.unparsedArgs?.isNotEmpty() == true){
                        context.sendRespond(
                            ResponseType.NORMAL,
                            EmbedFrame.error("잘못된 사용법","작성한 옵션 : ${res?.options?.map { it.value  }}\n올바른 사용법 :volume number OR -v number OR --volume=number"){
                                footer {
                                    text = "자세한 것은 help 참고"
                                }
                            }
                        )
                    }
                    else {
                        client.lavaClient.setVolume(context.guildId,volume)
                        context.sendRespond(
                            ResponseType.NORMAL,
                            EmbedFrame.info("음량이 $volume 으로 설정되었습니다",null)
                        )
                    }
                }
            }
        }
    }
}