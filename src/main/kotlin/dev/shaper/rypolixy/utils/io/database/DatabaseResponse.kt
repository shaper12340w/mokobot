package dev.shaper.rypolixy.utils.io.database

import dev.kord.common.entity.Snowflake
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import java.util.UUID

class DatabaseResponse {

    data class RawResponse(
        val status:DatabaseStatus,
        val data: MutableList<Map<String, Any?>>?,
        val message:String?
    )

    data class GuildResponse(
        val id:UUID,
        val discordId: Snowflake,
        val name: String,
        val users: List<UUID>,
        val allowedCommands: List<UUID>
    )

    data class UserResponse(
        val id:UUID,
        val discordId: Snowflake,
        val name: String,
        val email: String?,
        val language: String,
        val permissions: Int,
    )

    data class PlayerResponse(
        val userId:UUID,
        val volume:Int,
        val lyrics:Boolean,
        val platform: MediaUtils.MediaPlatform,
    )

    data class CommandResponse(
        val commandId:UUID,
        val commandName:String,
        val commandClass:String,
    )

    enum class DatabaseStatus {
        SUCCESS, FAILURE
    }
}