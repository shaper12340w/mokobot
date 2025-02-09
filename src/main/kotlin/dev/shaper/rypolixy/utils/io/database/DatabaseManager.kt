package dev.shaper.rypolixy.utils.io.database

import dev.kord.core.entity.Guild
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import kotlinx.coroutines.flow.toSet

object DatabaseManager{

    suspend fun registerAll(guild: Guild) {
        val users = guild.members.toSet()
        val userQuery = DatabaseQuery.userQuery(DatabaseQuery.QueryType.INSERT)
        val playerQuery = DatabaseQuery.playerQuery(DatabaseQuery.QueryType.INSERT)

        // Guild 초기화
        Database.initGuild(guild)

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
                connection.close()
            } catch (e: Exception) {
                connection.rollback() // 오류 발생 시 롤백
                throw e // 예외 다시 던지기
            }
        }

        // 등록된 사용자 ID 조회
        val userList = Database.execute("SELECT user_id FROM users").data?.toList()?.map { it["user_id"] } ?: return

        // 플레이어 등록
        Database.getConnection().use { connection ->
            connection.autoCommit = false // 트랜잭션 시작
            try {
                connection.prepareStatement(playerQuery).use { statement ->
                    for (user in userList) {
                        statement.setObject(1, user) // 사용자 ID
                        statement.setObject(2, 50) // 초기 값
                        statement.setObject(3, false) // 초기 값
                        statement.setObject(4, MediaUtils.MediaPlatform.YOUTUBE.toString()) // 플랫폼
                        statement.addBatch() // 배치에 추가
                    }
                    statement.executeBatch() // 배치 실행
                }
                connection.commit() // 트랜잭션 커밋
                connection.close()
            } catch (e: Exception) {
                connection.rollback() // 오류 발생 시 롤백
                throw e // 예외 다시 던지기
            }
        }
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