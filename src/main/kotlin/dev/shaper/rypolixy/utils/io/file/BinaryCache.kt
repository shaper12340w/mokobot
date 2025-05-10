package dev.shaper.rypolixy.utils.io.file

import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object BinaryCache {

    fun <K,T>saveBinaryCache(cache: Map<K, T>, file: File) {
        ObjectOutputStream(file.outputStream()).use {
            it.writeObject(cache)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <K,T>loadBinaryCache(file: File): Map<K, T> {
        if (!file.exists()) return emptyMap()
        else if(file.length() == 0L) return emptyMap()
        ObjectInputStream(file.inputStream()).use {
            return it.readObject() as Map<K, T>
        }
    }

}