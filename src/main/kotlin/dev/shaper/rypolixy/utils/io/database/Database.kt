package dev.shaper.rypolixy.utils.io.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.kord.common.entity.Permission
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.shaper.rypolixy.config.Properties
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import io.github.oshai.kotlinlogging.KotlinLogging
import org.intellij.lang.annotations.Language
import java.sql.SQLException
import java.sql.SQLTimeoutException

object Database {

    private val logger = KotlinLogging.logger {}

    private val hikariConfig = HikariConfig().apply {
        jdbcUrl         = Properties.getProperty("db.url")
        username        = Properties.getProperty("db.user")
        password        = Properties.getProperty("db.password")
        driverClassName = "org.postgresql.Driver"  // 사용하는 DB 드라이버에 맞게 설정
        maximumPoolSize = 10  // 최대 연결 수 설정
    }

    private val dataSource = HikariDataSource(hikariConfig)

    private fun getConnection(): Connection = dataSource.connection

    fun initTable() {

        @Language("postgresql")
        val query = """
        CREATE TABLE IF NOT EXISTS guilds (
            id      BIGSERIAL PRIMARY KEY,    -- auto incrementing id
            did     BIGINT NOT NULL,          -- discord id  
            name    VARCHAR(255) NOT NULL    -- guild name
        );
        CREATE TABLE IF NOT EXISTS users (
            id      BIGSERIAL PRIMARY KEY,      -- auto incrementing id
            did     BIGINT NOT NULL,            -- discord id
            name    VARCHAR(255) NOT NULL,      -- user name
            terms   BOOLEAN NOT NULL,           -- check terms of service 
            email   VARCHAR(255) UNIQUE,        -- email address
            level   SMALLINT NOT NULL           -- user level
        );
        """.trimIndent()
        logger.info { "Init tables" }
        val result = execute(query)
        if(result)
            logger.info { "Tables created" }
        else
            logger.warn { "Tables not created. Set it to defaults.." }

    }

    fun newGuild(guild:Guild) = runBlocking {
        @Language("postgresql")
        val query = """
            INSERT INTO guilds VALUES (DEFAULT, ?, ? )
        """.trimIndent()
        val result = execute(
            query,guild.id,         //id
            guild.name,             //name
        )
        if(result)
            logger.info { "User ${guild.name} added" }
        else
            logger.warn { "User ${guild.name} not added" }
    }

    fun newUser(user:User){

        @Language("postgresql")
        val query = """
            INSERT INTO 
                users
            VALUES 
                (DEFAULT,?,?,FALSE,NULL,0)
        """.trimIndent()
        val result = execute(query,user.id.toString().toLong(),user.username)
        if(result)
            logger.info { "User ${user.username} added" }
        else
            logger.warn { "User ${user.username} not added" }

    }

    private fun execute(sql: String,vararg values:Any?):Boolean{
        try {
            val result = getConnection().prepareStatement(sql).apply {
                if(values.isNotEmpty())
                    values.forEachIndexed { index, value ->
                        setObject(index+1,value)
                    }
            }.execute()
            logger.debug { "Successfully executed \"${sql.replace("\n","")}\"" }
            return result
        } catch (e:SQLException){
            logger.error { "Error while executing $sql" }
            logger.error { e.message }
            return false
        } catch (e:SQLTimeoutException){
            logger.error { "Timeout while executing $sql" }
            logger.error { e.message }
            return false
        }

    }

    /** deprecated
    inline fun <reified T : SqlBuilder> sql(init: T.() -> Unit): T {
        return T::class.java.getDeclaredConstructor().newInstance().apply(init)
    }
    */




}