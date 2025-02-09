package dev.shaper.rypolixy.config

import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.util.*

object Properties {
    private const val file          = "src/main/resources/settings.properties"
    private val tokens              = Properties()
    private val configFile          = File(file)

    init {
        if (!configFile.exists()) {
            throw FileNotFoundException("Config file not found")
        }
        tokens.load(FileReader(configFile))
    }


    fun getProperty(key: String): String? {
        return tokens.getOrDefault(key,null) as String?
    }
}