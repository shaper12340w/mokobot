package dev.shaper.rypolixy.utils.io.database

class DatabaseData {

    abstract class GuildDataStructure{
        abstract val guildData : DatabaseResponse.GuildResponse?
        abstract val playerData: DatabaseResponse.PlayerResponse?
    }

    abstract class UserDataStructure {
        abstract val userData: DatabaseResponse.UserResponse?
        abstract val statusData: DatabaseResponse.StatusResponse?
    }

    data class GuildDataInput(
        override val guildData : DatabaseResponse.GuildResponse?,
        override val playerData: DatabaseResponse.PlayerResponse?,
    ):GuildDataStructure()

    data class GuildDataReturn(
        override val guildData: DatabaseResponse.GuildResponse,
        override val playerData: DatabaseResponse.PlayerResponse,
    ):GuildDataStructure()

    data class UserDataInput(
        override val userData: DatabaseResponse.UserResponse?,
        override val statusData: DatabaseResponse.StatusResponse?,
    ):UserDataStructure()

    data class UserDataReturn(
        override val userData: DatabaseResponse.UserResponse,
        override val statusData: DatabaseResponse.StatusResponse,
    ):UserDataStructure()

}