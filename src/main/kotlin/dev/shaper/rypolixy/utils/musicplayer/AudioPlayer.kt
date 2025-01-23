package dev.shaper.rypolixy.utils.musicplayer

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.voice.AudioFrame
import java.util.concurrent.ConcurrentHashMap
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.musicplayer.lavaplayer.LavaPlayerManager
import dev.shaper.rypolixy.utils.musicplayer.ytdlp.YtDlpManager
import java.util.concurrent.TimeUnit


@OptIn(KordVoice::class)
class AudioPlayer(client:Client) {

    enum class SearchOptions(option: String) {
        YOUTUBE("ytsearch"),
        SOUNDCLOUD("scsearch"),
        SPOTIFY("spsearch"),
        URL("")
    }

    data class PlayerOptions(
        var leaveTime:      Long = 0,
        var volume:         Double = 100.0,
        var shuffle:        Boolean = false
    ){
        enum class RepeatType {
            DEFAULT, ONCE, ALL
        }
        var repeat: RepeatType = RepeatType.DEFAULT
    }

    data class ConnectOptions(
        val channel: ChannelBehavior,
        val voiceChannel: BaseVoiceChannelBehavior,
        val playerOptions: PlayerOptions
    )

    init {
        LavaPlayerManager.registerAllSources()
    }

    val sessions = ConcurrentHashMap<Snowflake, AudioSession>()

    suspend fun connect(channel: BaseVoiceChannelBehavior)
        = connect(ConnectOptions(channel.asChannel(), channel, PlayerOptions()))

    suspend fun connect(options:ConnectOptions) {
        val player  = LavaPlayerManager.createPlayer()
        val queue   : MutableList<AudioTrack> = mutableListOf()
        val guildId = options.voiceChannel.guildId

        val connection = options.voiceChannel.connect {
            audioProvider {
                player.provide(1, TimeUnit.SECONDS)?.let {
                    return@audioProvider AudioFrame.fromData(it.data)
                }
                playNext(guildId)
                return@audioProvider AudioFrame.SILENCE
            }
        }

        sessions[guildId] = AudioSession(
            queue,
            player,
            connection,
            options.playerOptions
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

    private suspend fun trackConverter(track: AudioTrack) {
        val trackURL = track.audioTrack.info.uri
        val data = YtDlpManager.getPlaylistData(trackURL)
        if(data != null) {
            //TODO : yt-dlp cover and playing
        }
    }

    suspend fun search(query: String): List<AudioTrack> = search(query,SearchOptions.SOUNDCLOUD)
    suspend fun search(query: String,option: SearchOptions?): List<AudioTrack> {
        suspend fun optionSearcher(query: String,option: SearchOptions): AudioTrack?{
            return AudioTrack.trackLoader("$option:$query").invoke()
        }
        val returnTracks = mutableListOf<AudioTrack>()
        if(queryChecker(query) != null || option == SearchOptions.URL){
            val tracks = AudioTrack.playlistLoader(query)
            returnTracks.addAll(tracks)
        }
        else{
            val track = when(option) {
                SearchOptions.YOUTUBE    -> optionSearcher(query,option)
                SearchOptions.SOUNDCLOUD -> optionSearcher(query,option)
                SearchOptions.SPOTIFY    -> {
                    //TODO
                    null
                }
                else -> {
                    null
                }
            }
            if(track != null) returnTracks.add(track)

        }
        logger.debug { "Track ${returnTracks.map { it.audioTrack.info.title }}" }
        return returnTracks
    }

    fun play(tracks: List<AudioTrack>,guildId: Snowflake) {
        if(tracks.isEmpty()) return
        if(!sessions.containsKey(guildId)) return
        val session = sessions[guildId]!!
        tracks.forEach{ track -> add(track,session) }
        if(session.currentTrack().status != AudioTrack.PlayStatus.PLAYING)
            session.currentTrack().playWith(sessions[guildId]!!.player)

    }

    private fun add(track: AudioTrack,session: AudioSession)
        = session.queue.add(track)

    private suspend fun playNext(guildId: Snowflake){
        logger.info { "Playing Next Track.." }
        val session = sessions[guildId] ?: return
        val leftTrack = session.queue.filter { it.status == AudioTrack.PlayStatus.IDLE }
        val leftTrackIndexes = leftTrack.map { session.queue.indexOf(it) }

        if(session.queue.isEmpty()) return

        if(session.options.repeat != PlayerOptions.RepeatType.ONCE)
            session.currentTrack().status = AudioTrack.PlayStatus.END //Play ended

        //if play all ended
        if(session.queue.isNotEmpty() && leftTrack.isEmpty())
            when (session.options.repeat) {
                PlayerOptions.RepeatType.DEFAULT -> disconnect(guildId)
                PlayerOptions.RepeatType.ALL     -> session.queue.forEach { it.status = AudioTrack.PlayStatus.IDLE }
                PlayerOptions.RepeatType.ONCE    -> null
            }

        if(session.options.repeat == PlayerOptions.RepeatType.DEFAULT) {
            if(session.options.shuffle)
                session.index = leftTrackIndexes.random()
            else
                session.index = leftTrackIndexes.first()
            if(session.currentTrack().status == AudioTrack.PlayStatus.END)
                session.currentTrack().clone()
            session.currentTrack().status = AudioTrack.PlayStatus.PLAYING
        }
        session.currentTrack().playWith(session.player)
    }

}