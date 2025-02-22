package dev.shaper.rypolixy.command.commands.mutual.player

import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.core.musicplayer.MediaOptions
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.context.ResponseType
import us.jimschubert.kopper.Parser

class Repeat(private val client: Client): MutualCommand {

    override val name           : String                    = "repeat"
    override val description    : String                    = "Repeat options for playing tracks"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override fun setup(builder: ChatInputCreateBuilder) {
        builder.apply {
            string("type","repeat type"){
                required = true
                choice("ALL","a")
                choice("ONCE","o")
                choice("NONE","n")
            }
        }
    }

    override fun setup(builder: Parser) {
        builder.apply {
            option("t",listOf("type"),"repeat Type")
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
            fun modeValue(option: String?): MediaOptions.RepeatType? {
                return when (option) {
                    "a","all","ALL","All"                           -> MediaOptions.RepeatType.ALL
                    "o","once","ONCE","Once"                        -> MediaOptions.RepeatType.ONCE
                    "n","none","default","d","Default","DEFAULT"    -> MediaOptions.RepeatType.DEFAULT
                    else                                            -> null
                }
            }
            fun modeText(option: MediaOptions.RepeatType): String {
                return when (option) {
                    MediaOptions.RepeatType.ALL     -> "전체 반복"
                    MediaOptions.RepeatType.ONCE    -> "한 곡 반복"
                    MediaOptions.RepeatType.DEFAULT -> "반복 안함"
                }
            }
            when (context){
                is ContextType.Interaction -> {
                    val option = context.value.interaction.command.strings["type"]
                    val mode    = modeValue(option)!!
                    session.options.repeat = mode
                    context.sendRespond(
                        ResponseType.NORMAL,
                        EmbedFrame.info("반복 모드가 **${modeText(mode)}** 으로 설정되었습니다",null)
                    )
                }
                is ContextType.Message  -> {
                    val option  = res!!.options.option("type")
                    val mode    = modeValue(option)
                    if(mode == null || res.options.unparsedArgs.isNotEmpty()){
                        context.sendRespond(
                            ResponseType.NORMAL,
                            EmbedFrame.error("잘못된 사용법","작성한 옵션 : ${res.options.map { it.value  }}\n올바른 사용법 : -t [once/all/none] OR --type=[once/all/none]"){
                                footer {
                                    text = "자세한 것은 help 참고"
                                }
                            }
                        )
                    }
                    else {
                        session.options.repeat = mode
                        context.sendRespond(
                            ResponseType.NORMAL,
                            EmbedFrame.info("반복 모드가 **${modeText(mode)}** 으로 설정되었습니다",null)
                        )
                    }
                }
            }
        }
    }
}