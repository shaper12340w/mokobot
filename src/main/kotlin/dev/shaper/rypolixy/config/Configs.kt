package dev.shaper.rypolixy.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.shaper.rypolixy.utils.io.file.FileManager

class Configs {

    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    data class YamlConfig(
        val io: IOConfig,
        val auth: AuthConfig,
        val app: AppConfig,
    )

    data class AuthConfig(
        val key: KeyConfig,
        val id: IdConfig,
    )

    data class IOConfig(
        val database: DBConfig,
        val cache: CacheConfig,
    )

    data class DBConfig(
        val url: String,
        val name: String,
        val username: String,
        val password: String,
    )

    data class CacheConfig(
        val file: String,
        val expire: ULong,
        val enabled: Boolean,
    )

    data class KeyConfig(
        val discord: String,
        val youtube: String,
        val spotify: String,
        var soundcloud: String?,
        val artiva: String
    )

    data class IdConfig(
        val spotify: String,
    )

    data class AppConfig(
        val register: String,
        val client: String,
        val server: String
    )

    fun loadConfig(): YamlConfig {
        val path = FileManager.checkFile("config.yaml")
        if(path != null)
            return mapper.readValue(path.toFile(), YamlConfig::class.java)
        throw IllegalArgumentException("config.yaml file not found")
    }


}