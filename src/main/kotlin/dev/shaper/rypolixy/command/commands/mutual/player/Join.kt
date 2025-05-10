package dev.shaper.rypolixy.command.commands.mutual.player

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
import dev.shaper.rypolixy.core.music.MediaOptions
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.embed.Colors
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.channel
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.getMember
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.context.ResponseType
import dev.shaper.rypolixy.utils.io.database.DatabaseManager
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
            defaultMemberPermissions = Permissions(Permission.SendMessages)
            boolean("silent","join voice room silently"){
                required = false
            }
        }
    }

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {

        val silentFlag = res?.options?.flag("silent") == true
        suspend fun errorMessage(text:String) {
            val type = if (silentFlag) ResponseType.NO_REPLY else ResponseType.NORMAL
            context.sendRespond(type, EmbedFrame.error(text,null))
        }
        try {
            val state = context.getMember().getVoiceState()
            if (state.getChannelOrNull() == null) {
                errorMessage("음성 채널에 입장해주세요")
            } else {
                val playerOption = DatabaseManager.getGuildData(context.guildId).playerData
                val findPlayer = client.lavaClient.sessions[context.guildId]
                val channel = state.getChannelOrNull()
                val asChannel = channel?.asChannel()
                if (findPlayer != null)
                    errorMessage("이미 입장해있습니다")
                else {
                    //TODO: Get Player Setting data from Database
                    client.lavaClient.connect(
                        MediaOptions.ChannelOptions(
                            messageChannel  = context.channel.asChannelOf(),
                            voiceChannel    = channel!!.asChannelOf(),
                        ),
                        MediaOptions.PlayerOptions(
                            volume = playerOption.volume.toDouble(),
                        )
                    )
                    if (!silentFlag ||
                        (context is ContextType.Interaction
                                && context.value.interaction.command.booleans["silent"] == false)
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
        catch (ex: Exception) {
            errorMessage("음성 채널에 입장해주세요")
        }

    }

}