package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
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
            fun modeValue(option: String?): MediaUtils.PlayerOptions.RepeatType? {
                return when (option) {
                    "a","all","ALL","All"                           -> MediaUtils.PlayerOptions.RepeatType.ALL
                    "o","once","ONCE","Once"                        -> MediaUtils.PlayerOptions.RepeatType.ONCE
                    "n","none","default","d","Default","DEFAULT"    -> MediaUtils.PlayerOptions.RepeatType.DEFAULT
                    else                                            -> null
                }
            }
            fun modeText(option: MediaUtils.PlayerOptions.RepeatType): String {
                return when (option) {
                    MediaUtils.PlayerOptions.RepeatType.ALL     -> "전체 반복"
                    MediaUtils.PlayerOptions.RepeatType.ONCE    -> "한 곡 반복"
                    MediaUtils.PlayerOptions.RepeatType.DEFAULT -> "반복 안함"
                }
            }
            when (context){
                is ContextType.Interaction -> {
                    val option = context.value.interaction.command.strings["type"]
                    val mode    = modeValue(option)!!
                    session.connector.options.repeat = mode
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
                        session.connector.options.repeat = mode
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