package dev.shaper.rypolixy.utils.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.voice.VoiceConnection
import dev.shaper.rypolixy.config.Client

class LavaClient(val client: Client) {

    private var playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val musicManagers: MutableMap<ULong, PlayerManager> = HashMap<ULong, PlayerManager>()

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    @OptIn(KordVoice::class)
    @Synchronized
    fun createAudioPlayer(config: Config): PlayerManager {
        val musicManager = musicManagers.getOrDefault(
            config.guild.id.value,
            PlayerManager(playerManager,this ,config)
        )
        musicManagers[config.guild.id.value] = musicManager
        return musicManager
    }

    fun getAudioPlayer(guildId: ULong): PlayerManager? {
        return musicManagers[guildId]
    }

}