package dev.shaper.rypolixy.utils.musicplayer.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume

//DefaultAudioPlayerManager 상속
object LavaPlayerManager: DefaultAudioPlayerManager() {

    fun registerAllSources() {
        AudioSourceManagers.registerLocalSource(this)
        registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        registerSourceManager(HttpAudioSourceManager())
    }

    suspend fun loadPlaylist(query: String): List<AudioTrack> = suspendCoroutine {
        loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = it.resume(listOf(track))
            override fun playlistLoaded(playlist: AudioPlaylist) = it.resume(playlist.tracks)
            override fun noMatches() = it.resume(emptyList())
            override fun loadFailed(exception: FriendlyException) {
                println("Load failed. Query: $query, reason: ${exception.stackTraceToString()}")
                it.resume(emptyList())
            }
        })
    }

    suspend fun loadTrack(query: String): AudioTrack? = loadPlaylist(query).firstOrNull()

}