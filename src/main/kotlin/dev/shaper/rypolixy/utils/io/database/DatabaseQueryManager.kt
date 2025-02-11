package dev.shaper.rypolixy.utils.io.database

object DatabaseQueryManager {

    enum class Entity {
        GUILD, USER, PLAYER, STATUS, COMMAND
    }

    enum class QueryType {
        INSERT,
        SELECT_INFO,
        SELECT_UUID,
        UPDATE // 추가
    }

    fun generateQuery(entity: Entity, type: QueryType): String {
        val template = when (entity) {
            Entity.GUILD    -> DatabaseQuery.Guild
            Entity.USER     -> DatabaseQuery.User
            Entity.PLAYER   -> DatabaseQuery.Player
            Entity.STATUS   -> DatabaseQuery.Status
            Entity.COMMAND  -> DatabaseQuery.Commands
        }

        return when (type) {
            QueryType.INSERT        -> template.initInsert
            QueryType.SELECT_INFO   -> template.selectInfo
            QueryType.SELECT_UUID   -> template.selectUuid
            QueryType.UPDATE        -> template.updateInfo
            else -> ""
        }
    }

}