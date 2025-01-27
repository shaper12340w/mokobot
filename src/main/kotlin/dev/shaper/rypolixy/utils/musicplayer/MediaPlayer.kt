package dev.shaper.rypolixy.utils.musicplayer

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
import java.util.concurrent.TimeUnit


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

    private suspend fun queryChecker(query: String): MediaUtils.MediaPlatform? {
        return MediaUtils.MediaPlatform.entries.firstOrNull { link ->
            link.url.any { it.matches(query.toRegex()) }
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
                    return when (val lavaResult  = LavaPlayerManager.load(query)) {
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
        if(session.currentData()?.status != MediaBehavior.PlayStatus.PLAYING)
            session.index = session.queue.size - 1
        session.currentData()?.playWith(sessions[guildId]!!.player)
        return session.currentTrack()
    }

    //add track on queue
    private suspend fun add(track: MediaTrack, session: MediaData){
        if(track.source == MediaUtils.MediaPlatform.YOUTUBE){
            logger.debug { "Found track's Platform is Youtube!" }
            session.queue.add(implementTrack(track)!!)
        }

        else
            session.queue.add(track)
    }


    /*fuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuckfuck*/
    private suspend fun playNext(guildId: Snowflake){
        //TODO : when invoke Error -> send TextChannel to Error message + leave
        //TODO : automatize track and playlist <- + flatTrack to normal TrackData
        val session = sessions[guildId] ?: return
        //val leftTrack = session.queue.filter { it.data.status == MediaBehavior.PlayStatus.IDLE }
        //val leftTrackIndexes = leftTrack.map { session.queue.indexOf(it) }

        //RepeatAll  -> Track + Playlist as Track / RepeatOnce -> (Track/Playlist) of one as Track / Nothing -> Track <=> Playlist
        //Shuffle    -> (Track -> (Track + Playlist)) or (Playlist -> Playlist)

        if(session.queue.isEmpty()) return
        logger.info { "Playing Next Track.." }

        suspend fun normalTrackProcess(){
            //filter only Track / FlatPlaylist
            val leftTrack = session.queue.filter {
                when(it){
                    is MediaTrack.Track     -> it.data.status == MediaBehavior.PlayStatus.IDLE
                    is MediaTrack.Playlist  -> it.tracks.firstOrNull { subTrack -> subTrack is MediaTrack.FlatTrack } != null
                    else                    -> false
                }
            }
            val leftTrackIndexes = leftTrack.map { session.queue.indexOf(it) }

            if(session.options.repeat != MediaUtils.PlayerOptions.RepeatType.ONCE)
                session.currentData()?.status = MediaBehavior.PlayStatus.END       //Normally Play ended

            //if play all ended
            if(session.queue.isNotEmpty() && leftTrack.isEmpty())
                when (session.options.repeat) {
                    MediaUtils.PlayerOptions.RepeatType.DEFAULT -> return disconnect(guildId)
                    MediaUtils.PlayerOptions.RepeatType.ALL     -> session.queue.forEach {
                        when(it){
                            is MediaTrack.Track     -> it.data.status = MediaBehavior.PlayStatus.IDLE
                            is MediaTrack.Playlist  -> it.tracks.forEach { track -> if(track is MediaTrack.Track) track.data.status = MediaBehavior.PlayStatus.IDLE }
                            else                    -> Unit
                        }
                    }
                    MediaUtils.PlayerOptions.RepeatType.ONCE    -> Unit
                }

            if(session.options.repeat == MediaUtils.PlayerOptions.RepeatType.DEFAULT) {
                if(session.options.shuffle)
                    session.index = leftTrackIndexes.random()
                else
                    session.index = leftTrackIndexes.first()
                if(session.currentData()?.status == MediaBehavior.PlayStatus.END)
                    session.currentData()?.clone()
                session.currentData()?.status = MediaBehavior.PlayStatus.PLAYING
            }
            session.currentData()?.playWith(session.player)

        }

        when(val currentMedia = session.queue[session.index]){
            is MediaTrack.Playlist  -> {
                /**
                 * Have to process
                 *  - shuffle and extra system and check it is all done
                 *  - when if it is FlatTrack -> change it to normal Track
                 */
                val currentTrack = currentMedia.tracks[session.subIndex]
                if(currentTrack is MediaTrack.FlatTrack) {
                    val result = implementTrack(currentTrack, currentTrack.source) ?: throw RuntimeException("Converted value is null")
                    currentMedia.tracks[session.subIndex] = result
                    logger.debug { "Loaded Track from Playlist $currentMedia"}
                }

                val leftTrack = currentMedia.tracks.filter { (it as MediaTrack.Track).data.status == MediaBehavior.PlayStatus.IDLE }
                val leftTrackIndexes = leftTrack.map { currentMedia.tracks.indexOf(it) }


                if(leftTrackIndexes.isNotEmpty()){
                    if(session.options.shuffle)
                        session.subIndex = leftTrackIndexes.random()
                    else
                        session.subIndex = leftTrackIndexes.first()
                }
                else{
                    //goto normal track process
                    normalTrackProcess()
                    session.subIndex = 0
                }

            }
            is MediaTrack.Track     -> normalTrackProcess()
            else                    -> Unit
        }

    }

}