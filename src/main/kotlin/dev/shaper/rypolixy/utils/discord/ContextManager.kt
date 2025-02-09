package dev.shaper.rypolixy.utils.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.shaper.rypolixy.command.types.ContextType
class ContextManager {

    companion object {
        val ContextType.context
            get() = when (this) {
                is ContextType.Interaction  -> this.value.interaction
                is ContextType.Message      -> this.value.message
            }
        val ContextType.channel
            get() = when (this) {
                is ContextType.Interaction  -> this.value.interaction.channel
                is ContextType.Message      -> this.value.message.channel
            }
        val ContextType.channelId
            get() = when (this) {
                is ContextType.Interaction  -> this.value.interaction.channelId
                is ContextType.Message      -> this.value.message.channelId
            }
        val ContextType.id
            get() = when (this) {
                is ContextType.Interaction  -> this.value.interaction.id
                is ContextType.Message      -> this.value.message.id
            }
        val ContextType.guildId
            get() = when (this) {
                is ContextType.Interaction  -> this.value.interaction.data.guildId.value!!
                is ContextType.Message      -> this.value.message.data.guildId.value!!
            }
        @Deprecated("Use getUser() instead", ReplaceWith("getUser()"))
        val ContextType.user
            get() = when (this) {
                is ContextType.Interaction  -> this.value.interaction.data.user.value
                is ContextType.Message      -> this.value.message.data.author
            }
        @Deprecated("Use getMember() instead", ReplaceWith("getMember()"))
        val ContextType.member
            get() = when (this) {
                is ContextType.Interaction -> Member(
                    this.value.interaction.data.member.value!!,
                    this.value.interaction.user.data,
                    this.value.kord
                )
                is ContextType.Message -> this.value.member
            }
        val ContextType.interaction
            get() = when (this) {
                is ContextType.Interaction -> this.value.interaction
                is ContextType.Message -> this.value.message.interaction
            }
        val ContextType.kord
            get() = when (this) {
                is ContextType.Interaction -> this.value.interaction.kord
                is ContextType.Message -> this.value.message.kord
            }
        val ContextType.message
            get() = when (this) {
                is ContextType.Interaction -> this.value.interaction.data.message.value
                is ContextType.Message -> this.value.message.data
            }
        val ContextType.applicationId
            get() = when (this) {
                is ContextType.Interaction -> this.value.interaction.applicationId
                is ContextType.Message -> this.value.message.applicationId
            }
        suspend fun ContextType.getGuild(id:Snowflake = guildId): Guild {
            return when (this){
                is ContextType.Interaction  -> this.value.kord.getGuild(id)
                is ContextType.Message      -> this.value.message.kord.getGuild(id)
            }
        }

        suspend fun ContextType.getUser(id:Snowflake = this.user!!.id): User
            = this.kord.getUser(id)!!

        suspend fun ContextType.getMember(id:Snowflake = this.user!!.id): Member
            = this.getGuild().getMember(id)

    }

}