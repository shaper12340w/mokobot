package dev.shaper.rypolixy.utils.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.kord.common.annotation.KordVoice
import dev.kord.core.exception.GatewayNotFoundException
import dev.kord.voice.VoiceConnection
import dev.kord.voice.AudioFrame

@OptIn(KordVoice::class)
class PlayerManager @OptIn(KordVoice::class) constructor(
    private val manager: AudioPlayerManager,
    private val lavaClient: LavaClient,
    private val config:Config,
    private var connection: VoiceConnection? = null
) {

    private val player: AudioPlayer = manager.createPlayer()
    val scheduler: TrackScheduler = TrackScheduler(player)
    var isPaused: Boolean
        get() = player.isPaused
        set(value) {
            player.isPaused = value
        }
    var volume: Int
        get() = player.volume
        set(value) {
            player.volume = value
        }
    var repeatMode: RepeatMode
        get() = scheduler.repeatMode
        set(value) {
            scheduler.repeatMode = value
        }

    init {
        player.addListener(scheduler)
        volume = config.startVolume
    }

    suspend fun connect():VoiceConnection {
        val connector = VoiceConnection(
            config.guild.gateway?: GatewayNotFoundException.voiceConnectionGatewayNotFound(config.guild.id),
            lavaClient.client.kord.selfId,
            config.voiceChannel?.id!!,
            config.guild.id
        ){
            audioProvider {
                AudioFrame.fromData(player.provide().data)
            }
        }
        connector.connect()
        connection = connector
        return connector
    }

    suspend fun disconnect() {
        if(connection != null){
            connection?.disconnect()
        }
    }


}