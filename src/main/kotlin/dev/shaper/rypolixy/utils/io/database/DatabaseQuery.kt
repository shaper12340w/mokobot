package dev.shaper.rypolixy.utils.io.database

import org.intellij.lang.annotations.Language

sealed class DatabaseQuery {

    companion object {
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
            excluded_command    uuid[] NOT NULL             -- excluded_command
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
            guild_id            uuid PRIMARY KEY,           -- uuid PRIMARY KEY
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
            discord_id          BIGINT UNIQUE,              -- discord id
            command_name        VARCHAR(255) NOT NULL,      -- command name
            class               VARCHAR(255) NOT NULL       -- language classification      
        )
        """.trimIndent()
    }

    abstract val initInsert: String
    abstract val updateInfo: String
    abstract val selectInfo: String
    abstract val selectUuid: String

    data object Guild    : DatabaseQuery() {
        @Language("postgresql")
        override val initInsert = """
            INSERT INTO guilds 
            VALUES (?, ?, ?, ARRAY[]::uuid[], ARRAY[]::uuid[]) 
            ON CONFLICT (discord_id) DO NOTHING;
        """.trimIndent()

        @Language("postgresql")
        override val updateInfo = """
            UPDATE guilds
            SET 
                name = ?,
                users = ?,
                excluded_command = ?
            WHERE guild_id = ?;
        """.trimIndent()

        @Language("postgresql")
        override val selectInfo = """
            SELECT * FROM guilds WHERE guild_id = ?;
        """.trimIndent()

        @Language("postgresql")
        override val selectUuid = """
            SELECT guild_id FROM guilds WHERE discord_id = ?;
        """.trimIndent()
    }

    data object User     : DatabaseQuery() {
        @Language("postgresql")
        override val initInsert = """
            INSERT INTO users 
            VALUES (?,?,?,NULL,?,0) 
            ON CONFLICT (discord_id) DO NOTHING;
        """.trimIndent()

        @Language("postgresql")
        override val updateInfo = """
            UPDATE users
            SET
                name = ?,
                email = ?,
                language = ?,
                permission = ?
            WHERE user_id = ?;
        """.trimIndent()

        @Language("postgresql")
        override val selectInfo = """
            SELECT * FROM users WHERE user_id = ?;
        """.trimIndent()

        @Language("postgresql")
        override val selectUuid = """
            SELECT user_id FROM users WHERE discord_id = ?;
        """.trimIndent()
    }

    data object Player   : DatabaseQuery() {
        @Language("postgresql")
        override val initInsert = """
            INSERT INTO players 
            VALUES (?,?,?,?) 
            ON CONFLICT (guild_id) DO NOTHING;
        """.trimIndent()

        @Language("postgresql")
        override val updateInfo = """
            UPDATE players
            SET
                volume = ?,
                lyrics = ?,
                default_platform = ?
            WHERE guild_id = ?;
        """.trimIndent()

        @Language("postgresql")
        override val selectInfo = """
            SELECT * FROM players WHERE guild_id = ?;
        """.trimIndent()


        override val selectUuid: String get() = ""
    }

    data object Status   : DatabaseQuery() {
        @Language("postgresql")
        override val initInsert = """
            INSERT INTO status 
            VALUES (?,?,?,?) 
            ON CONFLICT (user_id) DO NOTHING;
        """.trimIndent()

        @Language("postgresql")
        override val updateInfo = """
            UPDATE status
            SET
                attend = ?,
                point = ?,
                game = ?
            WHERE user_id = ?;
        """.trimIndent()

        @Language("postgresql")
        override val selectInfo = """
            SELECT * FROM status WHERE user_id = ?;
        """.trimIndent()


        override val selectUuid: String get() = ""
    }

    data object Commands : DatabaseQuery() {
        @Language("postgresql")
        override val initInsert = """
            INSERT INTO commands 
            VALUES (?,?,?,?) 
            ON CONFLICT (command_id) DO NOTHING;
        """.trimIndent()

        @Language("postgresql")
        override val updateInfo = """
            UPDATE commands
            SET
                command_name = ?,
                class = ?
            WHERE command_id = ?;
        """.trimIndent()

        @Language("postgresql")
        override val selectInfo = """
            SELECT * FROM commands WHERE command_id = ?;
        """.trimIndent()


        override val selectUuid: String get() = ""
    }

}