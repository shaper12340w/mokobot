package dev.shaper.rypolixy.utils.musicplayer

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.voice.AudioFrame
import java.util.concurrent.ConcurrentHashMap
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.utils.musicplayer.lavaplayer.LavaPlayerManager
import java.util.concurrent.TimeUnit


@OptIn(KordVoice::class)
class AudioPlayer(client:Client) {

    enum class SearchOption(option: String) {
        YOUTUBE("ytsearch:"),
        SOUNDCLOUD("scsearch:"),
        SPOTIFY("spsearch:"),
    }

    init {
        LavaPlayerManager.registerAllSources()
    }

    val sessions = ConcurrentHashMap<Snowflake, AudioSession>()

    suspend fun connect(channel: BaseVoiceChannelBehavior) = connect(channel,channel.asChannel())

    suspend fun connect(channel: BaseVoiceChannelBehavior,textChannel: ChannelBehavior) {
        val player  = LavaPlayerManager.createPlayer()
        val queue   : MutableList<AudioTrack> = mutableListOf()
        val guildId = channel.guildId

        val connection = channel.connect {
            audioProvider {
                player.provide(1, TimeUnit.SECONDS)?.let {
                    return@audioProvider AudioFrame.fromData(it.data)
                }
                //playNext(guildId)
                return@audioProvider AudioFrame.SILENCE
            }
        }

        sessions[guildId] = AudioSession(
            queue,
            player,
            connection,
            channel,
            textChannel
        )
    }

    suspend fun disconnect(guildId: Snowflake) {
        sessions.remove(guildId)?.let {
            it.player.stopTrack()
            it.queue.clear()
            it.connection.shutdown()
        }
    }

    private suspend fun queryChecker(query: String): AudioType.Link? {
        return AudioType.Link.entries.firstOrNull { link ->
            link.url.any { it.matches(query.toRegex()) }
        }
    }

    suspend fun search(query: String): List<AudioTrack?> = search(query,null)
    suspend fun search(query: String,option: SearchOption?): List<AudioTrack> {
        suspend fun optionSearcher(query: String,option: SearchOption): AudioTrack?{
            return AudioTrack.trackLoader("$option:$query").invoke()
        }
        val track = when(option) {
            SearchOption.YOUTUBE -> optionSearcher(query,option)
            SearchOption.SOUNDCLOUD -> optionSearcher(query,option)
            SearchOption.SPOTIFY -> {
                //TODO
                 null
            }
            else -> {
                null
            }
        }
        return listOf()
    }

    private suspend fun playNext(guildId: Snowflake){
        val session = sessions[guildId] ?: return

        if(session.queue.isEmpty())
            return disconnect(guildId)

        //if(session.queue.filter { it.status == AudioTrack.PlayStatus. })

    }

}