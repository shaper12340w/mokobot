package dev.shaper.rypolixy.utils.musicplayer

import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.voice.AudioFrame
import java.util.concurrent.ConcurrentHashMap
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.musicplayer.MediaUtils.Companion.implementTrack
import dev.shaper.rypolixy.utils.musicplayer.MediaUtils.Companion.lavaTrackBuilder
import dev.shaper.rypolixy.utils.musicplayer.lavaplayer.LavaPlayerManager
import dev.shaper.rypolixy.utils.musicplayer.lavaplayer.LavaResult
import dev.shaper.rypolixy.utils.musicplayer.ytdlp.YtDlpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.PageAttributes.MediaType
import java.util.concurrent.TimeUnit
import kotlin.math.log


@OptIn(KordVoice::class)
class MediaPlayer(client:Client) {

    init {
        LavaPlayerManager.registerAllSources()
    }

    val sessions = ConcurrentHashMap<Snowflake, MediaData>()

    suspend fun connect(channel: BaseVoiceChannelBehavior)
        = connect(MediaUtils.ConnectOptions(channel.asChannel(), channel, MediaUtils.PlayerOptions()))

    suspend fun connect(options: MediaUtils.ConnectOptions) {
        val player  = LavaPlayerManager.createPlayer()
        val queue   : MutableList<MediaTrack> = mutableListOf()
        val guildId = options.voiceChannel.guildId
        val connection = options.voiceChannel.connect {
            audioProvider {
                player.provide(1, TimeUnit.SECONDS)?.let {
                    return@audioProvider AudioFrame.fromData(it.data)
                }
                return@audioProvider AudioFrame.SILENCE
            }

        }

        player.addListener {
            if(it is TrackEndEvent){
                CoroutineScope(Dispatchers.IO).launch {
                    playNext(guildId)
                }
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

    private fun queryChecker(query: String): MediaUtils.MediaPlatform? {
        return MediaUtils.MediaPlatform.entries.firstOrNull { link ->
            link.url.any { it.toRegex().matches(query) }
        }
    }


    suspend fun search(query: String, option: MediaUtils.MediaPlatform = MediaUtils.MediaPlatform.SOUNDCLOUD): MediaUtils.SearchResult {

        try{
            when(option){
                MediaUtils.MediaPlatform.YOUTUBE -> {
                    val dlpSearch   = YtDlpManager.getSearchData(query, option)
                    val result      = MediaUtils.ytDlpTrackBuilder(dlpSearch,option)
                    return MediaUtils.SearchResult(
                        status  = MediaUtils.SearchType.SUCCESS,
                        data    = result
                    )
                }
                MediaUtils.MediaPlatform.UNKNOWN -> { //URL
                    //TODO : Add process with other websites
                    val checkQuery = queryChecker(query)
                    if (checkQuery != null) {
                        val dlpResult   = YtDlpManager.getUrlData(query)
                            ?: return MediaUtils.SearchResult(MediaUtils.SearchType.NORESULTS,null)
                        val result      = MediaUtils.ytDlpTrackBuilder(dlpResult, option)
                        return MediaUtils.SearchResult(
                            status      = MediaUtils.SearchType.SUCCESS,
                            data        = result
                        )
                    } else
                    return when (val lavaResult = LavaPlayerManager.load(query)) {
                        is LavaResult.Error     -> throw lavaResult.error
                        is LavaResult.NoResults -> MediaUtils.SearchResult(MediaUtils.SearchType.NORESULTS,null)
                        is LavaResult.Success   -> MediaUtils.SearchResult(MediaUtils.SearchType.SUCCESS,
                            lavaTrackBuilder(lavaResult.track,option))
                    }
                }
                else                            -> {
                    return when (val lavaResult  = LavaPlayerManager.optionSearcher(query, option)) {
                        is LavaResult.Error     -> throw lavaResult.error
                        is LavaResult.NoResults -> MediaUtils.SearchResult(MediaUtils.SearchType.NORESULTS, null)
                        is LavaResult.Success   -> MediaUtils.SearchResult(MediaUtils.SearchType.SUCCESS,
                            lavaTrackBuilder(lavaResult.track,option))
                    }
                }
            }
        }
        catch (ex: Exception){
            logger.warn(ex) { "Exception while searching $query" }
            return MediaUtils.SearchResult(
                MediaUtils.SearchType.ERROR,
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
        if(session.currentData()?.status != MediaBehavior.PlayStatus.PLAYING){
            session.index = session.queue.size - 1
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
            session.queue.add(implementTrack(track)!!)
        }
        else session.queue.add(track)
    }


    private suspend fun playNext(guildId: Snowflake){

        //TODO : when invoke Error -> send TextChannel to Error message + leave
        //TODO : automatize track and playlist <- + flatTrack to normal TrackData

        //val leftTrack = session.queue.filter { it.data.status == MediaBehavior.PlayStatus.IDLE }
        //val leftTrackIndexes = leftTrack.map { session.queue.indexOf(it) }

        //RepeatAll  -> Track + Playlist as Track / RepeatOnce -> (Track/Playlist) of one as Track / Nothing -> Track <=> Playlist
        //Shuffle    -> (Track -> (Track + Playlist)) or (Playlist -> Playlist)

        //순서 -> 다음 트랙 계산 -> 플레이리스트일 경우 플레이리스트 내부 / 트랙일 경우 다음 요소 검색
        // 일반 트랙일 경우, 플레이리스트일 경우
        // 플레이리스트일 경우 -> 구현됨 / 구현안됨


        val session = sessions[guildId] ?: return
        val originQueue = session.queue
        val tempQueue = session.queue.toMutableList()
        val shuffle = session.options.shuffle

        if (tempQueue.isEmpty()) return
        logger.debug { "Playing Next Track.. / Data : $session" }

        while(tempQueue.isNotEmpty()) {
            val nextIndex = when {
                session.subIndex > 0 -> session.index
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
                        nextMedia.data.status = MediaBehavior.PlayStatus.PLAYING
                        session.index = originQueue.indexOf(nextMedia)
                        logger.debug { "Play Track : $nextMedia" }
                        return nextMedia.data.playWith(sessions[guildId]!!.player)
                    }
                    else tempQueue.removeAt(nextIndex)
                }
                is MediaTrack.Playlist      -> {
                    logger.debug { "Track Type : Playlist" }
                    val idleTracks = nextMedia.tracks.filterIsInstance<MediaTrack.FlatTrack>()
                        .plus(nextMedia.tracks.filterIsInstance<MediaTrack.Track>()
                        .filter { it.data.status == MediaBehavior.PlayStatus.IDLE })
                    if(idleTracks.isNotEmpty()){
                        val subNextIndex    = if (shuffle) (idleTracks.indices).random() else 0
                        val nextTrack       = idleTracks[subNextIndex]
                        val originPlaylist  = (originQueue[session.index] as MediaTrack.Playlist).tracks
                        val originIndex     = originPlaylist.indexOf(nextTrack)
                        if (nextTrack is MediaTrack.FlatTrack) {
                            val convertedTrack = nextTrack.toTrack()
                            if(convertedTrack == null) {
                                logger.debug { "Skip Track Because Null: $nextMedia " }
                                tempQueue.removeAt(nextIndex)
                                return
                            }
                            originPlaylist[originIndex] = convertedTrack
                            session.subIndex = originIndex
                            convertedTrack.data.status = MediaBehavior.PlayStatus.PLAYING
                            logger.debug { "Play Converted Track: $convertedTrack" }
                            return convertedTrack.data.playWith(session.player)
                        }
                        if(session.subIndex == 0)
                            session.index = nextIndex
                        (nextTrack as MediaTrack.Track).data.status = MediaBehavior.PlayStatus.PLAYING
                        session.subIndex = originPlaylist.indexOf(nextTrack)
                        logger.debug { "Play Track : $nextTrack" }
                        return nextTrack.data.playWith(sessions[guildId]!!.player)
                    }
                    else {
                        if(session.subIndex > 0) session.subIndex = 0
                        tempQueue.removeAt(nextIndex)
                    }
                }
                else                        -> Unit
            }
        }

        // No tracks left to play
        session.index = 0
        session.subIndex = 0

        logger.debug { "Queue is empty, leaving voice channel" }
        disconnect(guildId)



    }

}