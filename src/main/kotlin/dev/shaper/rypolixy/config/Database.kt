package dev.shaper.rypolixy.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import mu.KotlinLogging

object Database {

    private val logger = KotlinLogging.logger{}

    private val hikariConfig = HikariConfig().apply {
        jdbcUrl         = Properties.getProperty("db.url")
        username        = Properties.getProperty("db.user")
        password        = Properties.getProperty("db.password")
        driverClassName = "org.postgresql.Driver"  // 사용하는 DB 드라이버에 맞게 설정
        maximumPoolSize = 10  // 최대 연결 수 설정
    }

    private val dataSource = HikariDataSource(hikariConfig)

    fun init(){
        initTable()
    }

    fun getConnection(): Connection = dataSource.connection

    fun initTable() {
        val connection = getConnection()
        // 테이블 이름을 직접 문자열 보간을 통해 쿼리에 삽입
        val query = """
        CREATE TABLE IF NOT EXISTS users (
            id SERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            terms BOOLEAN NOT NULL,
            email VARCHAR(255) UNIQUE NOT NULL
        );
        CREATE TABLE IF NOT EXISTS guilds (
            id SERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            admin VARCHAR(25)[]
        )
        """.trimIndent()

        try {
            val statement = connection.createStatement()
            statement.executeUpdate(query)
            statement.close()
        } catch (e: Exception) {
            logger.error { e.toString() }
        } finally {
            connection.close()
        }
    }

}