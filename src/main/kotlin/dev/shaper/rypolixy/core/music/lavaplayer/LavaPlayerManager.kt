package dev.shaper.rypolixy.core.music.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.*
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.core.music.utils.MediaUtils
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


//DefaultAudioPlayerManager 상속
object LavaPlayerManager: DefaultAudioPlayerManager() {

    fun registerAllSources() {
        val dataReader      = DefaultSoundCloudDataReader()
        val dataLoader      = DefaultSoundCloudDataLoader()
        val formatHandler   = DefaultSoundCloudFormatHandler()
        AudioSourceManagers.registerLocalSource(this)
        registerSourceManager(SoundCloudAudioSourceManager(
            true,
            dataReader,
            dataLoader,
            formatHandler,
            DefaultSoundCloudPlaylistLoader(dataLoader, dataReader, formatHandler)
        ))
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