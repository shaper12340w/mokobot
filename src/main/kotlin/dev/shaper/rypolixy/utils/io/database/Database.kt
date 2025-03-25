package dev.shaper.rypolixy.utils.io.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.shaper.rypolixy.command.types.CommandStructure
import dev.shaper.rypolixy.config.Configs
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import dev.shaper.rypolixy.utils.structure.ValueTransfer.toBigInteger
import java.sql.Connection
import io.github.oshai.kotlinlogging.KotlinLogging
import org.json.JSONObject
import java.math.BigInteger
import java.sql.SQLException
import java.sql.SQLTimeoutException
import java.util.UUID;

object Database {

    val logger = KotlinLogging.logger {}

    private val hikariConfig = HikariConfig().apply {
        jdbcUrl         = Configs.DB.url+"/"+Configs.DB.name
        username        = Configs.DB.username
        password        = Configs.DB.password
        driverClassName = "org.postgresql.Driver"  // 사용하는 DB 드라이버에 맞게 설정
        maximumPoolSize = 10  // 최대 연결 수 설정
    }

    private val dataSource = HikariDataSource(hikariConfig)

    fun getConnection(): Connection = dataSource.connection

    private fun handleMessage(
        type: DatabaseQueryManager.QueryType,
        entity: DatabaseQueryManager.Entity,
        response: DatabaseResponse.RawResponse
    ){
        val typeName = when (type) {
            DatabaseQueryManager.QueryType.INSERT -> "INIT"
            DatabaseQueryManager.QueryType.SELECT_INFO -> "INFO"
            DatabaseQueryManager.QueryType.SELECT_UUID -> "UUID"
            DatabaseQueryManager.QueryType.UPDATE -> "UPDATE"
        }
        val entityName = when (entity) {
            DatabaseQueryManager.Entity.GUILD       -> "GUILD"
            DatabaseQueryManager.Entity.COMMAND     -> "COMMAND"
            DatabaseQueryManager.Entity.USER        -> "USER"
            DatabaseQueryManager.Entity.PLAYER      -> "PLAYER"
            DatabaseQueryManager.Entity.STATUS      -> "STATUS"
        }
        if(response.status == DatabaseResponse.DatabaseStatus.SUCCESS)
            logger.info { "[Database][$typeName] : Successfully executed $entityName" }
        else
            logger.warn { "[Database][$typeName] : Cannot execute $entityName" }
    }

    fun checkOwner(){
        val result = execute(DatabaseQuery.ownerQuery,Configs.DB.name)
        when(result.status){
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(
                    result.data != null     &&
                    result.data.size > 0    &&
                    result.data[0]["owner"] == "shaper"
                    ){

                    logger.info { "[Database][Permission] : Checked Database Owner Successfully!" }
                    logger.info { result.data }
                }
                else {
                    logger.error { "[Database][Permission] : You are not Database Owner!" }
                    throw SQLException("[Database][Permission] : You are not Database Owner!")
                }
            }
            DatabaseResponse.DatabaseStatus.FAILURE -> {
                logger.error { result.message }
                throw SQLException("[Database][Permission] : Cannot check Database owner")
            }
        }
    }
    fun initTable() {
        logger.info { "[Database] : Init tables" }
        execute(DatabaseQuery.initQuery)
    }

    fun initGuild(guild:Guild):UUID {
        val type    = DatabaseQueryManager.QueryType.INSERT
        val entity  = DatabaseQueryManager.Entity.GUILD
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val uuid    = newUUID()
        val result = execute(
            query,
            uuid,
            guild.id.toString().toBigInteger(),               //id
            guild.name,                                     //name
        )
        handleMessage(type,entity,result)
        return uuid
    }
    fun getGuild(guildId:UUID):DatabaseResponse.GuildResponse? {

        val type    = DatabaseQueryManager.QueryType.SELECT_INFO
        val entity  = DatabaseQueryManager.Entity.GUILD
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result = execute(query, guildId)
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    throw SQLException("[Database] not found")
                else
                {
                    handleMessage(type,entity,result)
                    val guildData = result.data[0]
                    DatabaseResponse.GuildResponse(
                        guildData["guild_id"] as UUID,
                        Snowflake(guildData["discord_id"].toString()),
                        guildData["name"] as String,
                        ((guildData["users"] as org.postgresql.jdbc.PgArray).array as Array<*>).map { it as UUID },
                        ((guildData["excluded_command"] as org.postgresql.jdbc.PgArray).array as Array<*>).map { it as UUID }
                    )
                }
            }
            DatabaseResponse.DatabaseStatus.FAILURE -> {
                logger.error { result.message }
                null
            }
        }
    }
    fun getGuildUUID(guildId:Snowflake):UUID? {

        val type    = DatabaseQueryManager.QueryType.SELECT_UUID
        val entity  = DatabaseQueryManager.Entity.GUILD
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result = execute(query, guildId.toString().toBigInteger())
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    null
                else
                {
                    handleMessage(type,entity,result)
                    val guildData = result.data[0]
                    guildData["guild_id"] as UUID
                }
            }
            DatabaseResponse.DatabaseStatus.FAILURE -> {
                logger.error { result.message }
                null
            }
        }
    }
    fun setGuild(data: DatabaseResponse.GuildResponse){
        val type    = DatabaseQueryManager.QueryType.UPDATE
        val entity  = DatabaseQueryManager.Entity.GUILD
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result  = execute(query, data.name, data.users, data.excludedCommands,data.id)
        handleMessage(type,entity,result)
    }

    fun initUser(user:User):UUID {
        val type    = DatabaseQueryManager.QueryType.INSERT
        val entity  = DatabaseQueryManager.Entity.USER
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val uuid    = newUUID()
        val result = execute(
            query,
            uuid,
            user.id.toString().toBigInteger(),
            user.username,
            "KR1"
        )
        handleMessage(type,entity,result)
        return uuid
    }
    fun getUser(userId:UUID):DatabaseResponse.UserResponse? {
        val type    = DatabaseQueryManager.QueryType.SELECT_INFO
        val entity  = DatabaseQueryManager.Entity.USER
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result = execute(query, userId)
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    throw SQLException("[Database] not found")
                else
                {
                    handleMessage(type,entity,result)
                    val userData = result.data[0]
                    DatabaseResponse.UserResponse(
                        userData["user_id"] as UUID,
                        Snowflake(userData["discord_id"].toString()),
                        userData["name"] as String,
                        userData["email"] as String?,
                        userData["language"] as String,
                        userData["permission"] as Int
                    )
                }
            }
            DatabaseResponse.DatabaseStatus.FAILURE -> {
                logger.error { result.message }
                null
            }
        }
    }
    fun getUserUUID(userId:Snowflake):UUID? {
        val type    = DatabaseQueryManager.QueryType.SELECT_UUID
        val entity  = DatabaseQueryManager.Entity.USER
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result = execute(query, userId.toString().toBigInteger())
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    null
                else
                {
                    handleMessage(type,entity,result)
                    val guildData = result.data[0]
                    guildData["user_id"] as UUID
                }
            }
            DatabaseResponse.DatabaseStatus.FAILURE -> {
                logger.error { result.message }
                null
            }
        }
    }
    fun setUser(data:DatabaseResponse.UserResponse){
        val type    = DatabaseQueryManager.QueryType.UPDATE
        val entity  = DatabaseQueryManager.Entity.USER
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result  = execute(query, data.name, data.email, data.language, data.permissions, data.id)
        handleMessage(type,entity,result)
    }

    fun initPlayer(guildId:UUID,volume:Int,lyrics:Boolean,platform:MediaUtils.MediaPlatform) {
        val type    = DatabaseQueryManager.QueryType.INSERT
        val entity  = DatabaseQueryManager.Entity.PLAYER
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result = execute(query,guildId,volume,lyrics,platform.toString())
        handleMessage(type,entity,result)
    }
    fun getPlayer(guildId:UUID):DatabaseResponse.PlayerResponse? {
        val type    = DatabaseQueryManager.QueryType.SELECT_INFO
        val entity  = DatabaseQueryManager.Entity.PLAYER
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result  = execute(query, guildId)
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    throw SQLException("[Database] not found")
                else
                {
                    handleMessage(type,entity,result)
                    val playerData = result.data[0]
                    DatabaseResponse.PlayerResponse(
                        playerData["guild_id"] as UUID,
                        playerData["volume"] as Int,
                        playerData["lyrics"] as Boolean,
                        MediaUtils.checkPlatform(playerData["default_platform"] as String)
                    )
                }
            }
            DatabaseResponse.DatabaseStatus.FAILURE -> {
                logger.error { result.message }
                null
            }
        }
    }
    fun setPlayer(data:DatabaseResponse.PlayerResponse){
        val type    = DatabaseQueryManager.QueryType.UPDATE
        val entity  = DatabaseQueryManager.Entity.PLAYER
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result  = execute(query,data.volume,data.lyrics,data.platform.toString(),data.guildId)
        handleMessage(type,entity,result)
    }

    //TODO : Add process status and commands
    fun initStatus(id:UUID){
        val type    = DatabaseQueryManager.QueryType.INSERT
        val entity  = DatabaseQueryManager.Entity.STATUS
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result = execute(query,id,0,100,null)
        handleMessage(type,entity,result)
    }
    fun getStatus(userId:UUID):DatabaseResponse.StatusResponse? {
        val type    = DatabaseQueryManager.QueryType.SELECT_INFO
        val entity  = DatabaseQueryManager.Entity.STATUS
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result  = execute(query, userId)
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    throw SQLException("[Database] not found")
                else
                {
                    handleMessage(type,entity,result)
                    val statusData = result.data[0]
                    DatabaseResponse.StatusResponse(
                        statusData["user_id"] as UUID,
                        statusData["attend"] as Int,
                        statusData["point"] as BigInteger,
                        statusData["game"] as JSONObject,
                    )
                }
            }
            DatabaseResponse.DatabaseStatus.FAILURE -> {
                logger.error { result.message }
                null
            }
        }
    }

    fun initCommand(command:CommandStructure,id:Snowflake?,pkg:String){
        val type    = DatabaseQueryManager.QueryType.INSERT
        val entity  = DatabaseQueryManager.Entity.COMMAND
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result = execute(query, newUUID(),id?.value?.toBigInteger(),command.name,pkg)
        handleMessage(type,entity,result)
    }
    fun getCommand(commandID: UUID):DatabaseResponse.CommandResponse? {
        val type    = DatabaseQueryManager.QueryType.SELECT_INFO
        val entity  = DatabaseQueryManager.Entity.COMMAND
        val query   = DatabaseQueryManager.generateQuery(entity, type)
        val result  = execute(query, commandID)
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    throw SQLException("[Database] not found")
                else
                {
                    handleMessage(type,entity,result)
                    val commandData = result.data[0]
                    DatabaseResponse.CommandResponse(
                        commandData["command_id"] as UUID,
                        commandData["command_name"] as String,
                        commandData["class"] as String
                    )
                }
            }
            DatabaseResponse.DatabaseStatus.FAILURE -> {
                logger.error { result.message }
                null
            }
        }
    }

    fun newUUID():UUID = UUID.randomUUID();

    fun execute(sql: String, vararg values: Any?): DatabaseResponse.RawResponse {
        val myConnection = getConnection()
        try {
            myConnection.use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    if (values.isNotEmpty()) {
                        values.forEachIndexed { index, value ->
                            statement.setObject(index + 1, value)
                        }
                    }

                    val startTime = System.currentTimeMillis()
                    val result = statement.execute()
                    val executionTime = System.currentTimeMillis() - startTime

                    // PostgreSQL 연결에 Notice 핸들러 추가
                    fun getWarnings(): String {
                        val warnings = mutableListOf<String>()
                        var warning = statement.warnings
                        while (warning != null) {
                            warnings.add(warning.message ?: "")
                            warning = warning.nextWarning
                        }
                        return warnings.joinToString("\n")
                    }
                    val warnings = getWarnings()
                    val message = buildString {
                        if (warnings.isNotEmpty()) {
                            append(warnings)
                            append("\n")
                        }
                        append("Query returned successfully in $executionTime msec.")
                    }

                    if (result) {
                        // ResultSet도 use 블록으로 감싸서 자동으로 닫히게 함
                        statement.resultSet.use { resultSet ->
                            logger.debug { "PSQL : $message" }
                            logger.debug { "ResultSet: $resultSet" }

                            // ResultSet의 데이터를 List로 변환하여 저장
                            val rows = mutableListOf<Map<String, Any?>>()
                            val metadata = resultSet.metaData
                            val columnCount = metadata.columnCount

                            while (resultSet.next()) {
                                val row = mutableMapOf<String, Any?>()
                                for (i in 1..columnCount) {
                                    val columnName = metadata.getColumnName(i)
                                    row[columnName] = resultSet.getObject(i)
                                }
                                rows.add(row)
                            }
                            connection.close()
                            return DatabaseResponse.RawResponse(
                                status = DatabaseResponse.DatabaseStatus.SUCCESS,
                                data = rows,  // 변환된 데이터를 전달
                                message = message
                            )
                        }
                    } else {
                        connection.close()
                        return DatabaseResponse.RawResponse(
                            status = DatabaseResponse.DatabaseStatus.SUCCESS,
                            data = null,
                            message = message
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            myConnection.close()
            logger.error { "Error while executing SQL : $sql" }
            logger.error { e.message }
            return DatabaseResponse.RawResponse(
                status = DatabaseResponse.DatabaseStatus.FAILURE,
                data = null,
                message = e.message
            )
        } catch (e: SQLTimeoutException) {
            myConnection.close()
            logger.error { "Timeout while executing $sql" }
            logger.error { e.message }
            return DatabaseResponse.RawResponse(
                status = DatabaseResponse.DatabaseStatus.FAILURE,
                data = null,
                message = e.message
            )
        }
    }


    /** deprecated
    inline fun <reified T : SqlBuilder> sql(init: T.() -> Unit): T {
        return T::class.java.getDeclaredConstructor().newInstance().apply(init)
    }
    */




}