package dev.shaper.rypolixy.utils.io.database

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import dev.shaper.rypolixy.utils.io.database.Database.logger
import dev.shaper.rypolixy.utils.structure.CacheSystem
import kotlinx.coroutines.flow.toSet

object DatabaseManager{

    private val guildCache  = CacheSystem<Snowflake,DatabaseData.GuildDataReturn>(500,86400)    //24 hour
    private val userCache   = CacheSystem<Snowflake,DatabaseData.UserDataReturn>(3000,3600)      //1 hour

    private fun throwFailedGetter(): Nothing
            = throw NoSuchElementException("Cannot Get Data from Database")

    suspend fun registerAll(guild: Guild) {
        val users = guild.members.toSet()
        val userQuery   = DatabaseQueryManager.generateQuery(DatabaseQueryManager.Entity.USER, DatabaseQueryManager.QueryType.INSERT)

        // Guild 초기화
        val guildUUID = Database.initGuild(guild)
        Database.initPlayer(guildUUID,50,false,MediaUtils.MediaPlatform.YOUTUBE)
        // 사용자 등록
        Database.getConnection().use { connection ->
            connection.autoCommit = false // 트랜잭션 시작
            try {
                connection.prepareStatement(userQuery).use { statement ->
                    for (user in users) {
                        statement.setObject(1, Database.newUUID()) // UUID
                        statement.setObject(2, user.id.toString().toBigInteger()) // 사용자 ID
                        statement.setObject(3, user.username) // 사용자 이름
                        statement.setObject(4, "KR1") // 지역 코드
                        statement.addBatch() // 배치에 추가
                    }
                    statement.executeBatch() // 배치 실행
                }
                connection.commit() // 트랜잭션 커밋
                logger.info { "[Database][USER] : Successfully add ALL guild($guildUUID) users into Database" }
            } catch (e: Exception) {
                connection.rollback() // 오류 발생 시 롤백
                throw e // 예외 다시 던지기
            }
        }
    }

    fun fetchUserData(userId: Snowflake): DatabaseData.UserDataReturn {
        val userUUID    = Database.getUserUUID(userId)  ?: throwFailedGetter()
        val userData    = Database.getUser(userUUID)    ?: throwFailedGetter()
        val statusData  = Database.getStatus(userUUID)  ?: throwFailedGetter()
        return DatabaseData.UserDataReturn(
            userData, statusData
        )
    }
    fun getUserData(userId: Snowflake): DatabaseData.UserDataReturn {
        return userCache.get(userId) { fetchUserData(userId) }
    }
    fun setUserData(userId: Snowflake, data: DatabaseData.UserDataInput) {
        when {
            data.userData   != null -> Database.setUser(data.userData)
            data.statusData != null -> Unit // TODO: Database.setStatus
        }
        userCache.update(userId,fetchUserData(userId))
    }

    fun fetchGuildData(guildId: Snowflake): DatabaseData.GuildDataReturn {
        val guildUUID   = Database.getGuildUUID(guildId)    ?: throwFailedGetter()
        val guildData   = Database.getGuild(guildUUID)      ?: throwFailedGetter()
        val playerData  = Database.getPlayer(guildUUID)     ?: throwFailedGetter()
        return DatabaseData.GuildDataReturn(
            guildData, playerData
        )
    }
    fun getGuildData(guildId: Snowflake): DatabaseData.GuildDataReturn {
        return guildCache.get(guildId) { fetchGuildData(guildId) }
    }
    fun setGuildData(guildId: Snowflake, data: DatabaseData.GuildDataInput) {
        when {
            data.guildData      != null -> Database.setGuild(data.guildData)
            data.playerData     != null -> Database.setPlayer(data.playerData)
        }
        guildCache.update(guildId, fetchGuildData(guildId))
    }

}

/*
 TODO :
  Check DB User Permission -> Database.kt
  Manage Bot Administrator
  Manage Command
  Manage Auth
  Manage Volume
  Attend
  Language
  Save playlist
  etc..
*/
// Getter 에서는 그냥 Cashed Data 가져오고 Setter 에서는 업데이트
// + 자동 업데이트
// 나중에 Command Interacting 하면 자동으로 Cache 업뎃 하도록 할까?
// 물론 유저부분만
// 길드 부분은 그냥 GuildUpdate 쪽에서 처리하면 될듯.