package dev.shaper.rypolixy.core.music

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.voice.AudioFrame
import dev.shaper.rypolixy.config.Settings
import java.util.concurrent.ConcurrentHashMap
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import dev.shaper.rypolixy.core.music.utils.MediaUtils.Companion.implementTrack
import dev.shaper.rypolixy.core.music.utils.MediaUtils.Companion.lavaTrackBuilder
import dev.shaper.rypolixy.core.music.lavaplayer.LavaPlayerManager
import dev.shaper.rypolixy.core.music.lavaplayer.LavaResult
import dev.shaper.rypolixy.core.music.utils.MediaRegex
import dev.shaper.rypolixy.core.music.utils.MediaUtils
import dev.shaper.rypolixy.core.music.ytdlp.YtDlpManager
import dev.shaper.rypolixy.utils.io.database.DatabaseData
import dev.shaper.rypolixy.utils.io.database.DatabaseManager
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.TimeUnit


@OptIn(KordVoice::class)
class MediaPlayer {

    companion object {
        val logger = KotlinLogging.logger {}
    }

    init {
        LavaPlayerManager.registerAllSources()
    }

    val sessions = ConcurrentHashMap<Snowflake, MediaData>()

    suspend fun connect(channel: VoiceChannel)
        = connect(
            MediaOptions.ChannelOptions(channel, channel),
            MediaOptions.PlayerOptions()
        )

    suspend fun connect(
        channelOptions: MediaOptions.ChannelOptions,
        playerOptions: MediaOptions.PlayerOptions
    ) {
        val player      = LavaPlayerManager.createPlayer()
        val queue       : MutableList<MediaTrack> = mutableListOf()
        val guildId     = channelOptions.voiceChannel.guildId
        val connection  = channelOptions.voiceChannel.connect {
            audioProvider {
                if(
                    sessions[guildId] != null &&
                    sessions[guildId]!!.player.status == MediaOptions.PlayerStatus.PAUSED
                    )
                    return@audioProvider AudioFrame.SILENCE
                player.provide(1, TimeUnit.SECONDS)?.let {
                    return@audioProvider AudioFrame.fromData(it.data)
                }
                return@audioProvider null
            }
        }
        val provider    = connection.audioProvider

        sessions[guildId] = MediaData(
            queue       = MediaOptions.QueueOptions(queue),
            player      = MediaOptions.PlayerData(player,provider,connection),
            options     = playerOptions,
            channels    = channelOptions
        )

        player.addListener(MediaEvent(this,guildId))
    }

    suspend fun disconnect(guildId: Snowflake) {
        sessions.remove(guildId)?.let {
            it.player.audio.stopTrack()
            it.queue.tracks.clear()
            it.player.connection.shutdown()
        }
    }


    suspend fun search(query: String, option: MediaUtils.MediaPlatform): MediaOptions.SearchResult {

        try{
            val checkQuery = MediaRegex.checkAllRegex(query,MediaRegex.REGEX)
            if (checkQuery.isNotEmpty()) {
                val platform    = when(checkQuery.keys.toList().first().split(".")[0]){
                    "youtube"       -> MediaUtils.MediaPlatform.YOUTUBE
                    "spotify"       -> MediaUtils.MediaPlatform.SPOTIFY
                    "soundcloud"    -> MediaUtils.MediaPlatform.SOUNDCLOUD
                    else            -> MediaUtils.MediaPlatform.UNKNOWN
                }
                logger.debug { "Found matched regex keys : ${checkQuery.keys.toList()}" }
                val track = implementTrack(query, platform) ?: return MediaOptions.SearchResult(MediaOptions.SearchType.NORESULTS,null)
                return MediaOptions.SearchResult(
                    status      = MediaOptions.SearchType.SUCCESS,
                    data        = track
                )
            }
            when(option){
                MediaUtils.MediaPlatform.YOUTUBE -> {
                    val dlpSearch   = YtDlpManager.getSearchData(query, option) ?: return MediaOptions.SearchResult(
                        MediaOptions.SearchType.NORESULTS,null)
                    val result      = MediaUtils.ytDlpTrackBuilder(dlpSearch,option)
                    return MediaOptions.SearchResult(
                        status  = MediaOptions.SearchType.SUCCESS,
                        data    = result
                    )
                }
                MediaUtils.MediaPlatform.SOUNDCLOUD -> {
                    return when (val lavaResult  = LavaPlayerManager.optionSearcher(query, option)) {
                        is LavaResult.Error     -> throw lavaResult.error
                        is LavaResult.NoResults -> MediaOptions.SearchResult(MediaOptions.SearchType.NORESULTS, null)
                        is LavaResult.Success   -> MediaOptions.SearchResult(
                            MediaOptions.SearchType.SUCCESS,
                            lavaTrackBuilder(lavaResult.track,option))
                    }
                }
                MediaUtils.MediaPlatform.SPOTIFY -> {
                    throw FriendlyException("Spotify는 아직 지원 준비중입니다.\n죄송합니다", FriendlyException.Severity.COMMON, null)
                }
                MediaUtils.MediaPlatform.UNKNOWN -> { //URL
                    //TODO : Add process with other websites
                    val ytdlpData   = YtDlpManager.getUrlData(query) ?: return MediaOptions.SearchResult(MediaOptions.SearchType.NORESULTS,null)
                    val lavaData    = MediaUtils.ytDlpTrackBuilder(ytdlpData,option)
                    return MediaOptions.SearchResult(
                        status      = MediaOptions.SearchType.SUCCESS,
                        data        = lavaData
                    )
                }

            }
        }
        catch (ex: Exception){
            logger.warn(ex) { "Exception while searching $query" }
            return MediaOptions.SearchResult(
                MediaOptions.SearchType.ERROR(ex),
                null
            )
        }
    }

    //play a track immediately
    suspend fun play(tracks: MediaTrack, guildId: Snowflake):MediaTrack.Track? {
        val session = getSession(guildId)
        add(tracks,session)
        if(session.player.status == MediaOptions.PlayerStatus.IDLE){
            getTrackHandler(guildId).playNextTrack()
            return session.currentTrack()
        }
        else {
            return when(val media = session.queue.tracks.last()){
                is MediaTrack.Track -> return media
                else                -> null
            }
        }
    }

    //add track on queue
    private suspend fun add(track: MediaTrack, session: MediaData){
        if(track.source == MediaUtils.MediaPlatform.YOUTUBE){
            logger.debug { "Found track's Platform is Youtube!" }
            session.queue.tracks.add(implementTrack(track.url!!)!!)
        }
        else session.queue.tracks.add(track)
    }

    suspend fun next(guildId: Snowflake): MediaTrack.Track? {
        val session = getSession(guildId)
        session.player.audio.stopTrack()
        val handler = getTrackHandler(guildId)
        return handler.playNextTrack()
    }

    private fun baseOnOffFunction(
        guildId: Snowflake,
        property: MediaData.(Boolean?) -> Boolean
    ): Boolean {
        val session = getSession(guildId)
        val currentValue = session.property(null)
        val newValue = !currentValue
        session.property(newValue)
        return newValue
    }

    fun pause(guildId: Snowflake): Boolean? {
        val session = getSession(guildId)
        when (session.player.status) {
            MediaOptions.PlayerStatus.TERMINATED,
            MediaOptions.PlayerStatus.PAUSED -> {
                session.player.audio.isPaused   = false

                return false
            }
            MediaOptions.PlayerStatus.PLAYING -> {
                session.player.audio.isPaused   = true

                return true
            }
            else -> return null
        }
    }

    fun shuffle(guildId: Snowflake):Boolean
        = baseOnOffFunction(guildId) { if (it != null) { options.shuffle = it }; options.shuffle }

    fun relate(guildId: Snowflake):Boolean
        = baseOnOffFunction(guildId) { if (it != null) { options.recommendation = it }; options.recommendation }

    fun getPosition(guildId: Snowflake): Long {
        val session = sessions[guildId]
        return session?.currentTrack()?.data?.audioTrack?.position ?: 0
    }

    fun setVolume(guildId: Snowflake, volume:Int){
        val session = getSession(guildId)
        session.options.volume = volume.toDouble()
        session.player.audio.volume = volume
        val playerData = DatabaseManager.getGuildData(guildId).playerData.copy(volume = volume)
        DatabaseManager.setGuildData(
            guildId,
            DatabaseData.GuildDataInput(null,playerData)
        )
    }

    fun getVolume(guildId: Snowflake): Double {
        val session = getSession(guildId)
        return session.options.volume
    }

    suspend fun sendError(guildId: Snowflake,e: Exception){
        val session = getSession(guildId)
        session.channels.messageChannel.createMessage { embeds = mutableListOf(EmbedFrame.error("처리 도중 에러가 발생했습니다",e.message){ footer { text = "관리자에게 문의 바랍니다" } }) }
        Settings.printException(Thread.currentThread(),e)
    }

    private fun getTrackHandler(guildId: Snowflake)
        = MediaTrackHandler(this,guildId)

    private fun getSession(guildId: Snowflake) : MediaData
        = sessions[guildId] ?: throw Exception("Cannot get Session")

}