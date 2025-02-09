package dev.shaper.rypolixy.utils.io.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.shaper.rypolixy.command.types.CommandStructure
import dev.shaper.rypolixy.config.Configs
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import java.sql.Connection
import io.github.oshai.kotlinlogging.KotlinLogging
import org.intellij.lang.annotations.Language
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

    private fun handleMessage(type: String, name: String, response: DatabaseResponse.RawResponse){
        if(response.status == DatabaseResponse.DatabaseStatus.SUCCESS)
            logger.info { "[Database][$type] : Successfully add $name" }
        else
            logger.warn { "[Database][$type] : Cannot add $name" }
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
        val result = execute(DatabaseQuery.initQuery)
        handleMessage("Table","ALL",result)
    }

    fun initGuild(guild:Guild) {
        val result = execute(
            DatabaseQuery.guildQuery(DatabaseQuery.QueryType.INSERT),
            newUUID(),
            guild.id.toString().toBigInteger(),               //id
            guild.name,                                     //name
        )
        handleMessage("Guild",guild.name,result)
    }

    fun getGuild(guildId:UUID):DatabaseResponse.GuildResponse? {

        val result = execute(DatabaseQuery.guildQuery(DatabaseQuery.QueryType.SELECT_INFO), guildId)
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    throw SQLException("[Database] not found")
                else
                {
                    val guildData = result.data[0]
                    DatabaseResponse.GuildResponse(
                        guildData["guild_id"] as UUID,
                        Snowflake(guildData["discord_id"].toString()),
                        guildData["name"] as String,
                        ((guildData["users"] as org.postgresql.jdbc.PgArray).array as Array<*>).map { it as UUID },
                        ((guildData["allowed_command"] as org.postgresql.jdbc.PgArray).array as Array<*>).map { it as UUID }
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

        val result = execute(DatabaseQuery.guildQuery(DatabaseQuery.QueryType.SELECT_UUID), guildId.toString().toBigInteger())
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    null
                else
                {
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

    fun initUser(user:User) {

        val result = execute(
            DatabaseQuery.userQuery(DatabaseQuery.QueryType.INSERT),
            newUUID(),
            user.id.toString().toBigInteger(),
            user.username,
            "KR1"
        )
       handleMessage("User",user.username,result)
    }

    fun getUser(userId:UUID):DatabaseResponse.UserResponse? {

        val result = execute(DatabaseQuery.userQuery(DatabaseQuery.QueryType.SELECT_INFO), userId)
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    throw SQLException("[Database] not found")
                else
                {
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
        val result = execute(DatabaseQuery.userQuery(DatabaseQuery.QueryType.SELECT_UUID), userId.toString().toBigInteger())
        return when (result.status) {
            DatabaseResponse.DatabaseStatus.SUCCESS -> {
                if(result.data == null || result.data.size == 0)
                    null
                else
                {
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

    fun initPlayer(id:UUID,volume:Int,lyrics:Boolean,platform:MediaUtils.MediaPlatform) {
        val result = execute(DatabaseQuery.playerQuery(DatabaseQuery.QueryType.INSERT),id,volume,lyrics,platform.toString())
        handleMessage("Player",id.toString(),result)
    }

    fun initStatus(id:UUID){
        @Language("postgresql")
        val query = """
            INSERT INTO 
                status
            VALUES 
                (?,?,?,?)
        """.trimIndent()
        val result = execute(query,id,0,100,null)
        handleMessage("Status",id.toString(),result)
    }

    fun initCommand(command:CommandStructure,pkg:String){
        @Language("postgresql")
        val query = """
            INSERT INTO 
                commands
            VALUES 
                (?,?,?)
        """.trimIndent()
        val result = execute(query, newUUID(),command.name,pkg)
        handleMessage("Command",command.name,result)
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
            logger.error { "Error while executing $sql" }
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