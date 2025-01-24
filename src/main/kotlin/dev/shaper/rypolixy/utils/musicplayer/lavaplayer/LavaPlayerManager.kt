package dev.shaper.rypolixy.utils.musicplayer.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.musicplayer.MediaType
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume

//DefaultAudioPlayerManager 상속
object LavaPlayerManager: DefaultAudioPlayerManager() {

    fun registerAllSources() {
        AudioSourceManagers.registerLocalSource(this)
        registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        registerSourceManager(HttpAudioSourceManager())
    }

    suspend fun load(query: String): LookupResult = suspendCoroutine {
        logger.debug { "find query : $query" }
        loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack)             = it.resume(LookupResult.Success(track))
            override fun playlistLoaded(playlist: AudioPlaylist)    = it.resume(LookupResult.Success(playlist))
            override fun noMatches()                                = it.resume(LookupResult.NoResults)
            override fun loadFailed(exception: FriendlyException) {
                logger.error { "Load failed. Query: $query, reason: ${exception.stackTraceToString()}" }
                it.resume(LookupResult.Error)
            }
        })
    }

    suspend fun optionSearcher(query: String,option: MediaType.MediaSource): LookupResult{
        return load("${option.option}:$query")
    }


}