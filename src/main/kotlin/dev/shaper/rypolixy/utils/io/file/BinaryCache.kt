package dev.shaper.rypolixy.utils.io.file

import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object BinaryCache {

    fun saveBinaryCache(cache: Map<String, String>, file: File) {
        ObjectOutputStream(file.outputStream()).use {
            it.writeObject(cache)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadBinaryCache(file: File): Map<String, String> {
        if (!file.exists()) return emptyMap()
        ObjectInputStream(file.inputStream()).use {
            return it.readObject() as Map<String, String>
        }
    }

}