package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.message.EmbedBuilder
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.Colors
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.channel
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.getMember
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import us.jimschubert.kopper.Parser


class Join(private val client: Client): MutualCommand {

    override val name           : String                    = "join"
    override val description    : String                    = "Join a voice channel"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override fun setup(builder: Parser) {
        builder.apply {
            flag("silent",listOf(),"join voice room silently")
        }
    }

    override fun setup(builder: ChatInputCreateBuilder) {
        builder.apply {
            defaultMemberPermissions = Permissions(Permission.ManageMessages)
            boolean("silent","join voice room silently"){
                required = false
            }
        }
    }

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {

        suspend fun errorMessage(text:String) = context.sendRespond(ResponseType.NORMAL,EmbedFrame.error(text,null))
        try {
            val state = context.getMember().getVoiceState()
            if (state.getChannelOrNull() == null) {
                errorMessage("음성 채널에 입장해주세요")
            } else {
                val findPlayer = client.lavaClient.sessions[context.guildId]
                val channel = state.getChannelOrNull()
                val asChannel = channel?.asChannel()
                if (findPlayer != null)
                    errorMessage("이미 입장해있습니다")
                else {
                    //TODO: Get Player Setting data from Database
                    client.lavaClient.connect(
                        MediaUtils.ConnectOptions(
                            channel = context.channel.asChannelOf(),
                            voiceChannel = channel!!.asChannelOf(),
                            options = MediaUtils.PlayerOptions()
                        )
                    )
                    if (res?.options?.flag("silent") == true ||
                        (context is ContextType.Interaction
                                && context.value.interaction.command.booleans["silent"] == true)
                    ) {
                        context.sendRespond(ResponseType.NORMAL, EmbedBuilder().apply {
                            title = "🔊 Join"
                            description = "<#${asChannel!!.id}>에 입장했습니다"
                            color = Colors.YELLOW
                        })
                        logger.info { "Joined ${asChannel!!.name} voice channel" }
                    }

                }
            }
        }
        catch (ex: Exception) { errorMessage("음성 채널에 입장해주세요") }

    }

}