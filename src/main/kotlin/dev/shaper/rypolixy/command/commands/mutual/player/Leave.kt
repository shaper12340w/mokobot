package dev.shaper.rypolixy.command.commands.mutual.player

import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.rest.builder.message.EmbedBuilder
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.Colors
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.kord
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType


class Leave(private val client: Client): MutualCommand {

    override val name           : String                    = "leave"
    override val description    : String                    = "Leave a voice channel"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {

        val voiceChannel = context.kord.getSelf().asMember(context.guildId).getVoiceState().getChannelOrNull()
        if (voiceChannel == null)
            context.sendRespond(ResponseType.NORMAL,EmbedFrame.error("음성 채널에 있지 않습니다!",null))
        else{
            val findPlayer = client.lavaClient.sessions[context.guildId]
            if(findPlayer == null) {
                client.lavaClient.connect(voiceChannel.asChannel() as VoiceChannel)
            }
            client.lavaClient.disconnect(context.guildId)
            context.sendRespond(ResponseType.NORMAL, EmbedBuilder().apply {
                title = "🔊 Leave"
                description = "<#${voiceChannel.asChannel().id}>에서 퇴장했습니다"
                color = Colors.YELLOW
            })
        }

    }

}