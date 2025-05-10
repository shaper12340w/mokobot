package dev.shaper.rypolixy.utils.io.file

import dev.shaper.rypolixy.config
import dev.shaper.rypolixy.logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object CacheFile {

    val cache: MutableMap<String,CacheKeyOptions> = mutableMapOf()

    init {
        val path = FileManager.checkFile("cache.bin")
        if(path == null)
            Files.createFile(Paths.get("config","cache.bin"))
        loadCache()
        if(cache.isEmpty())
            logger.warn { "Cache file is empty" }
    }

    fun loadCache(){
        val path = FileManager.checkFile("cache.bin")
        if(path != null){
            val readFile = BinaryCache.loadBinaryCache<String,CacheKeyOptions>(path.toFile())
            for ((k, v) in readFile) {
                cache.putIfAbsent(k, v)
            }
        }
        else
            throw IllegalArgumentException("cache.bin file not found")
    }

    fun saveCache(name:String, path: File, options: CacheKeyOptions){
        cache[name] = options
        BinaryCache.saveBinaryCache(cache,path)
    }

    fun loadKey(name:String):String?{
        if(cache[name] != null)
            return cache[name]?.key
        return null
    }

    fun saveKey(name:String, key:String){
        val path = Paths.get("config","cache.bin").toAbsolutePath().toFile()
        saveCache(name,path,
            CacheKeyOptions(
                expireAfter     = config.io.cache.expire.toLong(),
                generateDate    = Date().time ,
                key             = key
            )
        )
    }

}