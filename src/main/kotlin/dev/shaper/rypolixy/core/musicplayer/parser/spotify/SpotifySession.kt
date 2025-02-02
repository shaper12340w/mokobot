//import com.spotify.protocol.client.*
//import com.spotify.protocol.types.*
//import dev.lavalink.youtube.clients.*
import java.io.Closeable
import java.io.IOException
import java.nio.file.Paths

class SpotifySession(
    private val username: String,
    private val password: String
){ //: Closeable {

//    private lateinit var session: SpotifySession
//    private var lastRenewTime = 0L
//
//    init {
//        connect()
//    }
//
//    fun connect(maxRetries: Int = 3) {
//        val config = SpotifyConfiguration.Builder()
//            .setUsername(username)
//            .setPassword(password)
//            .setDeviceType(DeviceType.COMPUTER)
//            .setDeviceName("KotlinBotV2")
//            .setReconnectPolicy(ReconnectPolicy.ALWAYS)
//            .build()
//
//        repeat(maxRetries) { attempt ->
//            try {
//                session = SpotifySession(config).apply {
//                    connect()
//                    if (!isValid) throw IOException("Invalid session")
//                }
//                lastRenewTime = System.currentTimeMillis()
//                return
//            } catch (e: Exception) {
//                if (attempt == maxRetries - 1) throw e
//                Thread.sleep(2000L * (attempt + 1))
//            }
//        }
//    }
//
//    fun renewSessionIfNeeded() {
//        if (System.currentTimeMillis() - lastRenewTime > TimeUnit.HOURS.toMillis(1)) {
//            try {
//                session.renew()
//                lastRenewTime = System.currentTimeMillis()
//            } catch (e: Exception) {
//                connect()
//            }
//        }
//    }
//
//    fun getEncryptedStream(uri: String): Pair<InputStream, ByteArray> {
//        renewSessionIfNeeded()
//        val trackId = SpotifyId.fromUri(uri)
//        return session.contentFeeder().load(trackId, AudioQuality.HIGH).inputStream to
//                CryptoUtil.decryptAudioKey(
//                    trackId.fileId,
//                    session.deviceId,
//                    session.audioKey
//                )
//    }
//
//    override fun close() = session.close()
}