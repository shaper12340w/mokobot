package dev.shaper.rypolixy.core.musicplayer

import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.voice.AudioFrame
import java.util.concurrent.ConcurrentHashMap
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils.Companion.implementTrack
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils.Companion.lavaTrackBuilder
import dev.shaper.rypolixy.core.musicplayer.lavaplayer.LavaPlayerManager
import dev.shaper.rypolixy.core.musicplayer.lavaplayer.LavaResult
import dev.shaper.rypolixy.core.musicplayer.parser.MediaParser
import dev.shaper.rypolixy.core.musicplayer.utils.MediaRegex
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import dev.shaper.rypolixy.core.musicplayer.ytdlp.YtDlpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@OptIn(KordVoice::class)
class MediaPlayer {

    init {
        LavaPlayerManager.registerAllSources()
    }

    val sessions = ConcurrentHashMap<Snowflake, MediaData>()

    suspend fun connect(channel: VoiceChannel)
        = connect(MediaUtils.ConnectOptions(channel, channel, MediaUtils.PlayerOptions()))

    suspend fun connect(options: MediaUtils.ConnectOptions) {
        val player      = LavaPlayerManager.createPlayer()
        val queue       : MutableList<MediaTrack> = mutableListOf()
        val guildId     = options.voiceChannel.guildId
        val connection  = options.voiceChannel.connect {
            audioProvider {
                if(sessions[guildId] != null && sessions[guildId]?.options?.paused == true)
                    return@audioProvider null
                player.provide(1, TimeUnit.SECONDS)?.let {
                    return@audioProvider AudioFrame.fromData(it.data)
                }
                return@audioProvider AudioFrame.SILENCE
            }
        }
        val provider    = connection.audioProvider

        player.addListener {
            CoroutineScope(Dispatchers.IO).launch {
                when (it) {
                    is TrackStuckEvent      -> {
                        logger.warn { "Track is Stuck" }
                    }
                    is TrackEndEvent        -> {
                        logger.debug { it.endReason }
                        val session = sessions[guildId] ?: return@launch
                        if(it.endReason == AudioTrackEndReason.CLEANUP && session.options.paused){
                            logger.warn { "Track is terminated! "}
                            session.options.terminated = true
                        }
                        else{
                            session.currentTrack()?.data?.status = MediaBehavior.PlayStatus.END
                            playNext(guildId)
                        }

                    }
                    is TrackStartEvent      -> {
                        val session = sessions[guildId] ?: return@launch
                        session.player.volume = session.connector.options.volume.toInt()
                    } //Todo : Add stack or deque to preload others
                    is TrackExceptionEvent  -> {
                        sendError(guildId,it.exception)
                        logger.error { it.exception }
                        disconnect(guildId)
                    }
                    is PlayerPauseEvent     -> {
                        val session = sessions[guildId] ?: return@launch
                        session.options.position    = session.currentTrack()?.data?.audioTrack?.position ?: 0
                        session.options.paused      = true
                    }
                    is PlayerResumeEvent    -> {
                        val session = sessions[guildId] ?: return@launch
                        if(session.options.terminated){
                            try { session.update() } //Try reload from saved url
                            catch (e:Exception){
                                try { session.reload() }  //Try re-search and reload
                                catch (ex:Exception) { throw ex } //else fuc stop it
                            }
                        }
                        session.options.paused = false
                    }

                }
            }
        }

        sessions[guildId] = MediaData(
            queue,
            player,
            provider,
            connection,
            options,
            MediaUtils.QueueOptions()
        )
    }

    suspend fun disconnect(guildId: Snowflake) {
        sessions.remove(guildId)?.let {
            it.player.stopTrack()
            it.queue.clear()
            it.connection.shutdown()
        }
    }


    suspend fun search(query: String, option: MediaUtils.MediaPlatform = MediaUtils.MediaPlatform.SOUNDCLOUD): MediaUtils.SearchResult {

        try{
            when(option){
                MediaUtils.MediaPlatform.YOUTUBE -> {
                    val dlpSearch   = YtDlpManager.getSearchData(query, option) ?: return MediaUtils.SearchResult(
                        MediaUtils.SearchType.NORESULTS,null)
                    val result      = MediaUtils.ytDlpTrackBuilder(dlpSearch,option)
                    return MediaUtils.SearchResult(
                        status  = MediaUtils.SearchType.SUCCESS,
                        data    = result
                    )
                }
                MediaUtils.MediaPlatform.SOUNDCLOUD -> {
                    return when (val lavaResult  = LavaPlayerManager.optionSearcher(query, option)) {
                        is LavaResult.Error     -> throw lavaResult.error
                        is LavaResult.NoResults -> MediaUtils.SearchResult(MediaUtils.SearchType.NORESULTS, null)
                        is LavaResult.Success   -> MediaUtils.SearchResult(
                            MediaUtils.SearchType.SUCCESS,
                            lavaTrackBuilder(lavaResult.track,option))
                    }
                }
                MediaUtils.MediaPlatform.SPOTIFY -> {
                    throw FriendlyException("Spotify는 아직 지원 준비중입니다.\n죄송합니다", FriendlyException.Severity.COMMON, null)
                }
                MediaUtils.MediaPlatform.UNKNOWN -> { //URL
                    //TODO : Add process with other websites
                    val checkQuery = MediaRegex.checkAllRegex(query,MediaRegex.REGEX)
                    if (checkQuery.isNotEmpty()) {
                        val platform    = when(checkQuery.keys.toList().first().split(".")[0]){
                            "youtube"       -> MediaUtils.MediaPlatform.YOUTUBE
                            "spotify"       -> MediaUtils.MediaPlatform.SPOTIFY
                            "soundcloud"    -> MediaUtils.MediaPlatform.SOUNDCLOUD
                            else            -> MediaUtils.MediaPlatform.UNKNOWN
                        }
                        logger.debug { "Found matched regex keys : ${checkQuery.keys.toList()}" }
                        val track = implementTrack(query, platform) ?: return MediaUtils.SearchResult(MediaUtils.SearchType.NORESULTS,null)
                        return MediaUtils.SearchResult(
                            status      = MediaUtils.SearchType.SUCCESS,
                            data        = track
                        )
                    } else {
                        val ytdlpData   = YtDlpManager.getUrlData(query) ?: return MediaUtils.SearchResult(MediaUtils.SearchType.NORESULTS,null)
                        val lavaData    = MediaUtils.ytDlpTrackBuilder(ytdlpData,option)
                        return MediaUtils.SearchResult(
                            status      = MediaUtils.SearchType.SUCCESS,
                            data        = lavaData
                        )
                    }
                }

            }
        }
        catch (ex: Exception){
            logger.warn(ex) { "Exception while searching $query" }
            return MediaUtils.SearchResult(
                MediaUtils.SearchType.ERROR(ex),
                null
            )
        }
    }

    //play a track immediately
    //Todo : Process unstable Track : FlatTrack
    suspend fun play(tracks: MediaTrack, guildId: Snowflake):MediaTrack.Track? {
        if(!sessions.containsKey(guildId)) return null
        val session = sessions[guildId]!!
        add(tracks,session)
        if(session.currentTrack()?.data?.status != MediaBehavior.PlayStatus.PLAYING){
            session.options.index = session.queue.size - 1
            playNext(guildId)
            return session.currentTrack()
        }
        else {
            return when(val media = session.queue.last()){
                is MediaTrack.Track -> return media
                else                -> null
            }
        }
    }

    //add track on queue
    private suspend fun add(track: MediaTrack, session: MediaData){
        if(track.source == MediaUtils.MediaPlatform.YOUTUBE){
            logger.debug { "Found track's Platform is Youtube!" }
            session.queue.add(implementTrack(track.url!!)!!)
        }
        else session.queue.add(track)
    }

    suspend fun next(guildId: Snowflake): MediaTrack.Track? {
        if(!sessions.containsKey(guildId)) return null
        val session = sessions[guildId]!!
        if(session.player.isPaused){
            session.player.isPaused = false
            playNext(guildId)
        }
        session.player.stopTrack()
        return session.currentTrack()
    }

    private fun baseOnOffFunction(
        guildId: Snowflake,
        property: MediaData.(Boolean?) -> Boolean
    ): Boolean? {
        if (!sessions.containsKey(guildId)) return null
        val session = sessions[guildId]!!
        val currentValue = session.property(null)
        val newValue = !currentValue
        session.property(newValue)
        return newValue
    }

    fun pause(guildId: Snowflake): Boolean?
        = baseOnOffFunction(guildId) { if (it != null) { player.isPaused = it }; player.isPaused }

    fun shuffle(guildId: Snowflake):Boolean?
        = baseOnOffFunction(guildId) { if (it != null) { connector.options.shuffle = it }; connector.options.shuffle }

    fun related(guildId: Snowflake):Boolean?
        = baseOnOffFunction(guildId) { if (it != null) { connector.options.recommendation = it }; connector.options.recommendation }

    fun volume(guildId: Snowflake, volume:Int){
        if(!sessions.containsKey(guildId)) return
        val session = sessions[guildId]!!
        session.connector.options.volume = volume.toDouble()
        session.player.volume = volume
    }

    private suspend fun sendError(guildId: Snowflake,e: Exception){
        if(!sessions.containsKey(guildId)) return
        val session = sessions[guildId]!!
        session.connector.channel.createMessage { embeds = mutableListOf(EmbedFrame.error("처리 도중 에러가 발생했습니다",e.message){ footer { text = "관리자에게 문의 바랍니다" } }) }
    }

    private suspend fun relateTrack(guildId: Snowflake){
        if(!sessions.containsKey(guildId)) return
        val session = sessions[guildId]!!
        if(session.current() != null && session.current()!! !is MediaTrack.FlatTrack){
            when(session.currentTrack()!!.source){
                MediaUtils.MediaPlatform.YOUTUBE,
                MediaUtils.MediaPlatform.SOUNDCLOUD -> {
                    val baseTrack = when(val track = session.currentBaseTrack()!!){
                        is MediaTrack.FlatTrack -> track.toTrack()
                        is MediaTrack.Track     -> track
                        else                    -> return
                    }
                    val parsedTrack = MediaParser.parse(baseTrack!!)
                    session.queue.addAll(parsedTrack!!)
                    session.connector.channel.createMessage {
                        embeds = mutableListOf(
                            EmbedFrame.list(
                                "추천 트랙이 추가되었습니다",
                                parsedTrack.joinToString("\n") { it.title }
                            )
                        )
                    }
                }
                else -> {
                    session.connector.channel.createMessage {
                        embeds = mutableListOf(
                            EmbedFrame.warning(
                                "지원하지 않는 미디어 플렛폼입니다",
                                "추천 기준 소스 : ${session.currentTrack()?.source?.name}\n링크 : ${session.currentTrack()?.url?.take(50)}"
                            )
                            { footer { text = "지원하지 않는 소스기에 자동으로 무시되었습니다." } }
                        )
                    }
                    session.connector.options.recommendation = false
                }
            }
        }
    }

    private suspend fun playNext(guildId: Snowflake): MediaTrack.Track? {

        fun playTrack(track: MediaTrack.Track): MediaTrack.Track {
            track.data.status = MediaBehavior.PlayStatus.PLAYING
            logger.debug { "Play Track : $track" }
            track.data.playWith(sessions[guildId]!!.player)
            return track
        }
        suspend fun playFlatTrack(track: MediaTrack.FlatTrack): MediaTrack.Track? {
            val convertedTrack = track.toTrack() ?: return null
            return playTrack(convertedTrack)
        }

        try {

            val session     = sessions[guildId] ?: return null
            val originQueue = session.queue
            val tempQueue   = session.queue.toMutableList()
            val shuffle     = session.connector.options.shuffle

            if (tempQueue.isEmpty()) return null
            logger.debug { "Playing Next Track.. / Data : $session" }

            if (session.connector.options.repeat == MediaUtils.PlayerOptions.RepeatType.ONCE){
                session.update()
                session.currentTrack()?.data?.playWith(sessions[guildId]!!.player)
                logger.debug { "Play Track : ${session.currentTrack()}" }
                return session.currentTrack()
            }

            //if (session.option.repeat == MediaUtils.PlayerOptions.RepeatType.DEFAULT)
            while(tempQueue.isNotEmpty()) {
                val nextIndex = when {
                    session.options.subIndex > 0 -> session.options.index
                    shuffle              -> (tempQueue.indices).random()
                    else                 -> 0
                }
                if (nextIndex !in tempQueue.indices) {
                    logger.warn { "Invalid nextIndex: $nextIndex in tempQueue size ${tempQueue.size}" }
                    tempQueue.clear()
                    break
                }
                val nextMedia = tempQueue[nextIndex]
                logger.debug { nextMedia.toString() }
                when(nextMedia) {
                    is MediaTrack.Track         -> {
                        logger.debug { "Track Type : Track" }
                        if(nextMedia.data.status == MediaBehavior.PlayStatus.IDLE){
                            session.options.index = originQueue.indexOf(nextMedia)
                            return playTrack(nextMedia)
                        }
                        else tempQueue.removeAt(nextIndex)
                    }
                    is MediaTrack.FlatTrack     -> {
                        logger.debug { "Track Type : FlatTrack" }
                        val convertedTrack = playFlatTrack(nextMedia)
                        if(convertedTrack == null) {
                            logger.debug { "Skip Track Because Null: $nextMedia " }
                            tempQueue.removeAt(nextIndex)
                            return null
                        }
                        session.options.index = originQueue.indexOf(nextMedia)
                        session.queue[session.options.index] = convertedTrack
                        return convertedTrack
                    }
                    is MediaTrack.Playlist      -> {
                        logger.debug { "Track Type : Playlist" }
                        val idleTracks = nextMedia.tracks.filterIsInstance<MediaTrack.FlatTrack>()
                            .plus(nextMedia.tracks.filterIsInstance<MediaTrack.Track>()
                                .filter { it.data.status == MediaBehavior.PlayStatus.IDLE })
                        if(idleTracks.isNotEmpty()){
                            val subNextIndex    = if (shuffle) (idleTracks.indices).random() else 0
                            val nextTrack       = idleTracks[subNextIndex]
                            val originPlaylist  = (originQueue[session.options.index] as MediaTrack.Playlist).tracks
                            val originIndex     = originPlaylist.indexOf(nextTrack)
                            if(session.options.subIndex == 0)
                                session.options.index = originQueue.indexOf(nextMedia)
                            if (nextTrack is MediaTrack.FlatTrack) {
                                val convertedTrack = playFlatTrack(nextTrack)
                                if(convertedTrack == null) {
                                    logger.debug { "Skip Track Because Null: $nextMedia " }
                                    tempQueue.removeAt(nextIndex)
                                    return null
                                }
                                originPlaylist[originIndex] = convertedTrack
                                session.options.subIndex = originIndex
                                return convertedTrack
                            } else if (nextTrack is MediaTrack.Track){
                                session.options.subIndex = originIndex
                                return playTrack(nextTrack)
                            }
                        }
                        else {
                            if(session.options.subIndex > 0) session.options.subIndex = 0
                            tempQueue.removeAt(nextIndex)
                        }
                    }
                    else                        -> throw IllegalStateException("Invalid nextIndex: $nextIndex in tempQueue size ${tempQueue.size}")
                }
            }

            if(session.connector.options.recommendation){
                relateTrack(guildId)
                playNext(guildId)
                return session.currentTrack()
            }

            // No tracks left to play
            session.options.index       = 0
            session.options.subIndex    = 0

            if(session.connector.options.repeat == MediaUtils.PlayerOptions.RepeatType.ALL){
                //Reset ALL Tracks
                session.queue.forEach {
                    when(it){
                        is MediaTrack.Track     -> it.data = it.data.clone()
                        is MediaTrack.Playlist  -> it.tracks.forEach { track -> (track as MediaTrack.Track).data = track.data.clone() }
                        else                    -> Unit
                    }
                }
                return playNext(guildId)
            }

            logger.debug { "Queue is empty, leaving voice channel" }
            disconnect(guildId)
            return null
        }
        catch (ex: Exception){
            sendError(guildId,ex)
            logger.error (ex) { ex.message }
            disconnect(guildId)
            return null
        }

    }

}