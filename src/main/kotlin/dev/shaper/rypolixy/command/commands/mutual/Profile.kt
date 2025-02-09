package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.rest.Image
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.user
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.route.CdnUrl
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.getMember
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.getUser
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType

class Profile (private val client: Client): MutualCommand {

    override val name           : String                    = "profile"
    override val description    : String                    = "Show users profile"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override fun setup(builder: ChatInputCreateBuilder) {
        builder.apply {
            defaultMemberPermissions = Permissions(Permission.ManageMessages)
            user("user","user for check profile")
        }
    }

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        suspend fun getImg(id:Snowflake) : String? {
            return context.getUser(id).avatar?.cdnUrl?.toUrl {
                CdnUrl.UrlFormatBuilder().apply {
                    format = Image.Format.WEBP
                    size = Image.Size.Size4096
                }
            } + "?size=4096"
        }
        when(context) {
            is ContextType.Interaction -> {
                val user = context.value.interaction.command.users.values.firstOrNull()
                ?: context.value.interaction.user
                val img  = getImg(user.id) ?: ""
                context.sendRespond(
                    ResponseType.NORMAL,
                    EmbedBuilder().apply {
                        title = "${user.username}님의 아바타에요"
                        image = img
                    }
                )
            }
            is ContextType.Message  -> {
                val userId = context.value.message.mentionedUserIds.firstOrNull()
                ?: context.value.message.referencedMessage?.author?.id
                ?: context.value.message.author?.id
                val img  = getImg(userId!!) ?: ""
                context.sendRespond(
                    ResponseType.NORMAL,
                    EmbedBuilder().apply {
                        title = "${context.getMember(userId).username}님의 아바타에요"
                        image = img
                    }
                )
            }
        }
    }

}