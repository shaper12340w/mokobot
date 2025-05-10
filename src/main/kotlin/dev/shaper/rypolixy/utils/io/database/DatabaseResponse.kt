package dev.shaper.rypolixy.utils.io.database

import dev.kord.common.entity.Snowflake
import dev.shaper.rypolixy.core.music.utils.MediaUtils
import org.json.JSONObject
import java.math.BigInteger
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
        val excludedCommands: List<UUID>
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
        val guildId:UUID,
        val volume:Int,
        val lyrics:Boolean,
        val platform: MediaUtils.MediaPlatform,
    )

    data class CommandResponse(
        val commandId:UUID,
        val commandName:String,
        val commandClass:String,
    )

    data class StatusResponse(
        val userId:UUID,
        val attendanceCount:Int,
        val points:BigInteger,
        val gameValue: JSONObject,
        //TODO : Add process to handle game Value
    )

    enum class DatabaseStatus {
        SUCCESS, FAILURE
    }
}