package dev.shaper.rypolixy.utils.musicplayer

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.voice.AudioFrame
import java.util.concurrent.ConcurrentHashMap
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.musicplayer.lavaplayer.LavaPlayerManager
import dev.shaper.rypolixy.utils.musicplayer.lavaplayer.LookupResult
import java.util.concurrent.TimeUnit


@OptIn(KordVoice::class)
class MediaPlayer(client:Client) {

    init {
        LavaPlayerManager.registerAllSources()
    }

    val sessions = ConcurrentHashMap<Snowflake, MediaData>()

    suspend fun connect(channel: BaseVoiceChannelBehavior)
        = connect(MediaType.ConnectOptions(channel.asChannel(), channel, MediaType.PlayerOptions()))

    suspend fun connect(options: MediaType.ConnectOptions) {
        val player  = LavaPlayerManager.createPlayer()
        val queue   : MutableList<MediaTrack.Track> = mutableListOf()
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

        sessions[guildId] = MediaData(
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

    private suspend fun queryChecker(query: String): MediaType.MediaSource? {
        return MediaType.MediaSource.entries.firstOrNull { link ->
            link.url.any { it.matches(query.toRegex()) }
        }
    }

    private suspend fun trackConverter(track: MediaTrack):MediaTrack? {
        when(track) {
            is MediaTrack.Track ->{
             null
            }
            else -> null
        }
        return null
    }

    suspend fun search(query: String): MediaType.SearchResult? = search(query,MediaType.MediaSource.SOUNDCLOUD)

    suspend fun search(query: String, option: MediaType.MediaSource?): MediaType.SearchResult? {

        val searchOptions = option ?: MediaType.MediaSource.SOUNDCLOUD
        val result = if(searchOptions != MediaType.MediaSource.UNKNOWN)
            LavaPlayerManager.optionSearcher(query,searchOptions)
        else
            LavaPlayerManager.load(query)
        if(result is LookupResult.Success){
            //TODO : Add process via trackConverter
            val track   = result.track
            val data    = MediaType.trackBuilder(track,searchOptions)
            return MediaType.SearchResult(result,data!!)
        }
        else
            return MediaType.SearchResult(result,null)

    }

    fun play(tracks: List<MediaTrack.Track>, guildId: Snowflake) {
        if(tracks.isEmpty()) return
        if(!sessions.containsKey(guildId)) return
        val session = sessions[guildId]!!
        tracks.forEach{ track -> add(track,session) }
        if(session.currentTrack().status != MediaBehavior.PlayStatus.PLAYING)
            session.currentTrack().playWith(sessions[guildId]!!.player)

    }

    private fun add(track: MediaTrack.Track, session: MediaData)
        = session.queue.add(track)

    private suspend fun playNext(guildId: Snowflake){

        val session = sessions[guildId] ?: return
        val leftTrack = session.queue.filter { it.data.status == MediaBehavior.PlayStatus.IDLE }
        val leftTrackIndexes = leftTrack.map { session.queue.indexOf(it) }

        if(session.queue.isEmpty()) return
        logger.info { "Playing Next Track.." }

        if(session.options.repeat != MediaType.PlayerOptions.RepeatType.ONCE)
            session.currentTrack().status = MediaBehavior.PlayStatus.END //Play ended

        //if play all ended
        if(session.queue.isNotEmpty() && leftTrack.isEmpty())
            when (session.options.repeat) {
                MediaType.PlayerOptions.RepeatType.DEFAULT -> return disconnect(guildId)
                MediaType.PlayerOptions.RepeatType.ALL     -> session.queue.forEach { it.data.status = MediaBehavior.PlayStatus.IDLE }
                MediaType.PlayerOptions.RepeatType.ONCE    -> null
            }

        if(session.options.repeat == MediaType.PlayerOptions.RepeatType.DEFAULT) {
            if(session.options.shuffle)
                session.index = leftTrackIndexes.random()
            else
                session.index = leftTrackIndexes.first()
            if(session.currentTrack().status == MediaBehavior.PlayStatus.END)
                session.currentTrack().clone()
            session.currentTrack().status = MediaBehavior.PlayStatus.PLAYING
        }
        session.currentTrack().playWith(session.player)
    }

}