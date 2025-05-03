package dev.shaper.rypolixy.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.nio.file.Paths

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
        val paths = listOf(
            Paths.get("config.yaml").toAbsolutePath(),
            Paths.get("src", "main", "resources", "config.yaml").toAbsolutePath(),
        )
        for (path in paths) {
            if (path.toFile().exists())
                return mapper.readValue(path.toFile(), YamlConfig::class.java)
        }
        throw IllegalArgumentException("config.yaml not found")
    }

//    val PROGRAMS = ProgramConfig(
//        ytdlp   = when(System.getProperty("os.name")){
//            "Linux"         -> Properties.getProperty("program.linux.ytdlp")    ?: warn("program.linux.ytdlp")
//            "Windows 11"    -> Properties.getProperty("program.windows.ytdlp")  ?: warn("program.windows.ytdlp")
//            else            -> warn("program.unknown.ytdlp")
//        }
//    )


}