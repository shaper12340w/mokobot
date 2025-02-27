package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.user
import dev.kord.rest.builder.message.EmbedBuilder
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.discord.KordUtils.avatarUrl
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.getMember
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.getUser
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.context.ResponseType


class Profile (private val client: Client): MutualCommand {

    override val name           : String                    = "profile"
    override val description    : String                    = "Show users profile"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override fun setup(builder: ChatInputCreateBuilder) {
        builder.apply {
            defaultMemberPermissions = Permissions(Permission.SendMessages)
            user("user","user for check profile")
        }
    }

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {

        when(context) {
            is ContextType.Interaction -> {
                val user = context.value.interaction.command.users.values.firstOrNull()
                ?: context.value.interaction.user
                val img  = context.getUser(user.id).avatarUrl()
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
                val img  = context.getUser(userId!!).avatarUrl()
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