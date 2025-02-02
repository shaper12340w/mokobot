package dev.shaper.rypolixy.core.musicplayer.parser.spotify

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SpotifyStreamProcessor(
    private val encryptedStream: InputStream,
    private val audioKey: ByteArray
) : Closeable {

    companion object {
        private const val IV_SIZE = 16
        private const val OGG_HEADER_SIZE = 58
        private val log = LoggerFactory.getLogger(SpotifyStreamProcessor::class.java)
    }

    private lateinit var cipher: Cipher
    private var ivRead = false
    private var oggHeaderRead = false

    fun init(): InputStream {
        return object : InputStream() {
            override fun read(): Int = throw IOException("Not supported")

            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                if (!ivRead) {
                    readIvAndInitCipher()
                }
                return processChunk(buffer, offset, length)
            }
        }
    }

    private fun readIvAndInitCipher() {
        val iv = ByteArray(IV_SIZE).apply {
            encryptedStream.read(this)
        }
        cipher = Cipher.getInstance("AES/CTR/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(audioKey, "AES"), IvParameterSpec(iv))
        }
        ivRead = true
    }

    private fun processChunk(buffer: ByteArray, offset: Int, length: Int): Int {
        val encryptedChunk = ByteArray(length).apply {
            val read = encryptedStream.read(this)
            if (read == -1) return -1
        }

        val decrypted = cipher.update(encryptedChunk)
        System.arraycopy(decrypted, 0, buffer, offset, decrypted.size)

        if (!oggHeaderRead) {
            validateOggHeader(decrypted)
            oggHeaderRead = true
        }
        return decrypted.size
    }

    private fun validateOggHeader(data: ByteArray) {
        if (data.size < OGG_HEADER_SIZE || data[0] != 0x4F.toByte()) {
            throw FriendlyException("Invalid Ogg header", FriendlyException.Severity.FAULT, null)
        }
    }

    override fun close() = encryptedStream.close()
}
