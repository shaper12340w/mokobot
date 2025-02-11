package dev.shaper.rypolixy.utils.io.database

import java.util.UUID

class DatabaseData {

    data class GuildData(
        val guildData: DatabaseResponse.GuildResponse,
    )

    data class UserData(
        val userData: DatabaseResponse.UserResponse,
        val playerData: DatabaseResponse.PlayerResponse,
        val statusData: DatabaseResponse.StatusResponse,
    )

}