package dev.shaper.rypolixy.utils.io.database

import org.intellij.lang.annotations.Language

object DatabaseQuery {

    @Language("postgresql")
    val ownerQuery = """
            SELECT datname AS database_name, 
                   pg_get_userbyid(datdba) AS owner
            FROM pg_database
            WHERE datname = ?
        """.trimIndent()

    @Language("postgresql")
    val initQuery = """
        CREATE TABLE IF NOT EXISTS guilds (
            guild_id            uuid PRIMARY KEY,           -- uuid PRIMARY KEY
            discord_id          BIGINT UNIQUE NOT NULL,     -- discord id  
            name                VARCHAR(255) NOT NULL,      -- guild name
            users               uuid[] NOT NULL,            -- users uuid
            allowed_command     uuid[] NOT NULL             -- allowed_command
        );
        CREATE TABLE IF NOT EXISTS users (
            user_id             uuid PRIMARY KEY,           -- uuid PRIMARY KEY
            discord_id          BIGINT UNIQUE NOT NULL,     -- discord id
            name                VARCHAR(255) NOT NULL,      -- user name        
            email               VARCHAR(255) UNIQUE,        -- email address
            language            CHAR(3) NOT NULL,           -- language
            permission          INTEGER NOT NULL            -- permissions                                            
        );
        CREATE TABLE IF NOT EXISTS players (
            user_id             uuid PRIMARY KEY,           -- uuid PRIMARY KEY
            volume              INTEGER NOT NULL,           -- volume
            lyrics              BOOLEAN NOT NULL,           -- lyrics
            default_platform    VARCHAR(255) NOT NULL       -- default platform
        );
        CREATE TABLE IF NOT EXISTS status (
            user_id             uuid PRIMARY KEY,           -- uuid PRIMARY KEY
            attend              INTEGER NOT NULL,           -- attendance count
            point               BIGINT NOT NULL,            -- user points
            game                json                        -- game data
        );
        CREATE TABLE IF NOT EXISTS commands (
            command_id          uuid PRIMARY KEY,           -- uuid PRIMARY KEY
            command_name        VARCHAR(255) NOT NULL,      -- command name
            class               VARCHAR(255) NOT NULL       -- language classification      
        )
        """.trimIndent()

    fun guildQuery(type:QueryType):String{
        return when (type) {
            QueryType.INSERT -> {
                @Language("postgresql")
                val query = """
                    INSERT INTO guilds VALUES (?, ?, ?, ARRAY[]::uuid[], ARRAY[]::uuid[])
                    ON CONFLICT (discord_id) DO NOTHING;
                """.trimIndent()
                query
            }
            QueryType.SELECT_INFO -> {
                @Language("postgresql")
                val query = """
                    SELECT *
                    FROM guilds
                    WHERE guild_id = ?
                """.trimIndent()
               query
            }
            QueryType.SELECT_UUID -> {
                @Language("postgresql")
                val query = """
                    SELECT guild_id
                        FROM guilds
                    WHERE discord_id = ?
                """.trimIndent()
                query
            }
            else -> ""
        }
    }

    fun userQuery(type:QueryType):String{
        return when (type) {
            QueryType.INSERT -> {
                @Language("postgresql")
                val query = """
                    INSERT INTO 
                        users
                    VALUES 
                        (?,?,?,NULL,?,0)
                    ON CONFLICT (discord_id) DO NOTHING;
                """.trimIndent()
                query
            }
            QueryType.SELECT_INFO -> {
                @Language("postgresql")
                val query = """
                    SELECT *
                    FROM users
                    WHERE user_id = ?
                """.trimIndent()
                query
            }
            QueryType.SELECT_UUID -> {
                @Language("postgresql")
                val query = """
                    SELECT user_id
                        FROM users
                    WHERE discord_id = ?
                """.trimIndent()
                query
            }
            else -> ""
        }
    }

    fun playerQuery(type:QueryType):String{
        return when (type) {
            QueryType.INSERT -> {
                @Language("postgresql")
                val query = """
                    INSERT INTO 
                        players
                    VALUES 
                        (?,?,?,?)
                    ON CONFLICT (user_id) DO NOTHING;
                """.trimIndent()
                query
            }
            QueryType.SELECT_INFO -> {
                ""
            }
            QueryType.SELECT_UUID -> {
                ""
            }
            else -> ""
        }
    }

    enum class QueryType{
        INSERT, SELECT_INFO, SELECT_UUID, UPDATE, DELETE
    }

}