package dev.shaper.rypolixy.utils.io.file

import java.nio.file.Path
import java.nio.file.Paths

object FileManager {

    fun checkFile(fileName:String): Path? {
        val paths = listOf(
            Paths.get(fileName).toAbsolutePath(),
            Paths.get("config",fileName).toAbsolutePath(),
            Paths.get("src", "main", "resources", fileName).toAbsolutePath(),
        )
        for (path in paths) {
            if (path.toFile().exists())
                return path
        }
        return null
    }
}