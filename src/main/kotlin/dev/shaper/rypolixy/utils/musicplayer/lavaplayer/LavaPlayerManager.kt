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
import dev.shaper.rypolixy.utils.musicplayer.utils.MediaUtils
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume

//DefaultAudioPlayerManager 상속
object LavaPlayerManager: DefaultAudioPlayerManager() {

    fun registerAllSources() {
        AudioSourceManagers.registerLocalSource(this)
        registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        registerSourceManager(HttpAudioSourceManager())
    }

    suspend fun load(query: String): LavaResult = suspendCoroutine {
        logger.debug { "find query : $query" }
        loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack)             = it.resume(LavaResult.Success(track))
            override fun playlistLoaded(playlist: AudioPlaylist)    = it.resume(LavaResult.Success(playlist))
            override fun noMatches()                                = it.resume(LavaResult.NoResults)
            override fun loadFailed(exception: FriendlyException) {
                logger.error { "Load failed. Query: $query, reason: ${exception.stackTraceToString()}" }
                it.resume(LavaResult.Error(exception))
            }
        })
    }

    suspend fun optionSearcher(query: String,option: MediaUtils.MediaPlatform): LavaResult{
        return load("${option.option}:$query")
    }


}