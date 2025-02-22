package dev.shaper.rypolixy.core.musicplayer

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.shaper.rypolixy.core.musicplayer.MediaPlayer.Companion.logger
import dev.shaper.rypolixy.core.musicplayer.parser.MediaParser
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import java.util.*

class MediaTrackHandler(player: MediaPlayer, private val guildId: Snowflake) {

    val session = player.sessions[guildId] ?: throw Exception("Session not found")
    val tracks  = session.queue.tracks

    private fun playTrack(track: MediaTrack.Track) {
        track.data.status       = MediaBehavior.PlayStatus.PLAYING
        session.player.status   = MediaOptions.PlayerStatus.PLAYING
        track.data.playWith(session.player.audio)
        logger.info { "[Player] Guild($guildId) Playing Track : $track" }
    }

    suspend fun getRelateTrack(track:MediaTrack.Track){
        when(track.source){
            MediaUtils.MediaPlatform.YOUTUBE,
            MediaUtils.MediaPlatform.SOUNDCLOUD -> {
                val parsedTrack = MediaParser.parse(track,3)
                    ?: throw Exception("Cannot parse Related tracks")
                session.queue.tracks.addAll(parsedTrack)
                session.channels.messageChannel.createMessage {
                    embeds = mutableListOf(
                        EmbedFrame.list(
                            "추천 트랙이 추가되었습니다",
                            parsedTrack.joinToString("\n") { it.title }
                        )
                    )
                }
            }
            else -> {
                session.channels.messageChannel.createMessage {
                    embeds = mutableListOf(
                        EmbedFrame.warning(
                            "지원하지 않는 미디어 플렛폼입니다",
                            "추천 기준 소스 : ${session.currentTrack().source.name}\n링크 : ${session.currentTrack().url?.take(50)}"
                        )
                        { footer { text = "지원하지 않는 소스이므로 자동으로 무시되었습니다." } }
                    )
                }
                session.options.recommendation = false
            }
        }
    }

    private fun resetSession() {
        session.queue.index     = 0
        session.queue.subIndex  = 0
    }

    private fun resetAllTracks() {
        session.queue.tracks.forEach { media ->
            when (media) {
                is MediaTrack.Track -> media.data = media.data.clone()
                is MediaTrack.Playlist -> media.tracks.forEach { track ->
                    (track as MediaTrack.Track).data = track.data.clone()
                }
                else -> Unit
            }
        }
    }


    suspend fun playNextTrack(): MediaTrack.Track? {
        val availableTracks = playableTrack()
        if (session.options.repeat == MediaOptions.RepeatType.ONCE) {
            val track = session.currentTrack()
            session.update()
            playTrack(track)
            return track
        }
        if (session.options.repeat == MediaOptions.RepeatType.ALL) {
            if (availableTracks.isEmpty()) {
                resetSession()
                resetAllTracks()
                return playNextTrack()
            }
        }
        if (session.options.repeat == MediaOptions.RepeatType.DEFAULT) {
            if (session.options.recommendation && availableTracks.isEmpty()) {
                getRelateTrack(session.currentTrack())
                return playNextTrack()
            }
        }
        if(handleNextTrack()){
            resetSession()
            return null
        }
        else return session.currentTrack()
    }

    private fun playableTrack(): List<MediaTrack> {
        val currentTracks   = session.queue.tracks
        val availableTracks = mutableListOf<MediaTrack>()
        currentTracks.forEach { track ->
            when (track) {
                is MediaTrack.FlatTrack -> availableTracks.add(track)
                is MediaTrack.Track     -> {
                    if(track.data.status == MediaBehavior.PlayStatus.IDLE)
                        availableTracks.add(track)
                }
                is MediaTrack.Playlist  -> {
                    val idleTracks = track.tracks.filterIsInstance<MediaTrack.FlatTrack>() // Collect FlatTracks (Not Implemented)
                        .plus(track.tracks.filterIsInstance<MediaTrack.Track>()
                            .filter { it.data.status == MediaBehavior.PlayStatus.IDLE })
                    availableTracks.addAll(idleTracks)
                }
                else -> Unit
            }
        }
        return availableTracks
    }

    private suspend fun handleNextTrack(): Boolean {
        val currentTracks   = session.queue.tracks                 // Base Track List
        val availableTracks = mutableListOf<MediaTrack>()           // Shallow Copy

        if(session.options.shuffle){
            currentTracks.forEach { track ->
                when (track) {
                    is MediaTrack.FlatTrack,
                    is MediaTrack.Track -> availableTracks.add(track)
                    is MediaTrack.Playlist -> {
                        availableTracks.add(
                            track.copy(tracks = track.tracks.shuffled().toMutableList())
                        )
                    }
                    else -> Unit
                }
            }
            availableTracks.shuffle()
        }
        else availableTracks.addAll(currentTracks)

        while (availableTracks.isNotEmpty()) {
            val nextIndex = if (session.queue.subIndex > 0)
                session.queue.index
            else 0
            when (val nextMedia = availableTracks[nextIndex]) {
                is MediaTrack.Track -> {
                    if(nextMedia.data.status == MediaBehavior.PlayStatus.IDLE) { //Check Track is Played
                        session.queue.index = currentTracks.indexOf(nextMedia)
                        playTrack(nextMedia)
                        return false
                    }
                    else availableTracks.removeAt(nextIndex)
                }
                is MediaTrack.FlatTrack -> {
                    val convertedTrack  = nextMedia.toTrack() ?: throw Exception("Error occurred while converting track")
                    val trackIndex      = currentTracks.indexOf(nextMedia)
                    session.queue.tracks[trackIndex] = convertedTrack
                    session.queue.index = trackIndex
                    playTrack(convertedTrack)
                    return false
                }
                is MediaTrack.Playlist -> {
                    val idleTracks = nextMedia.tracks.filterIsInstance<MediaTrack.FlatTrack>() // Collect FlatTracks (Not Implemented)
                        .plus(nextMedia.tracks.filterIsInstance<MediaTrack.Track>()
                            .filter { it.data.status == MediaBehavior.PlayStatus.IDLE }) // Collect Track Not Played
                    if (idleTracks.isNotEmpty()) {
                        val currentPlaylist = currentTracks[session.queue.index] as MediaTrack.Playlist
                        val nextTrack = idleTracks.first()
                        var implementedTrack: MediaTrack.Track? = null
                        if(session.queue.subIndex == 0)
                            session.queue.index = currentTracks.indexOf(nextMedia)
                        if(nextTrack is MediaTrack.FlatTrack){
                            implementedTrack = nextTrack.toTrack() ?: throw Exception("Error occurred while converting track")
                            currentPlaylist.tracks[session.queue.subIndex] = implementedTrack
                        }
                        session.queue.subIndex = currentPlaylist.tracks.indexOf(implementedTrack ?: nextTrack)
                        playTrack(implementedTrack ?: nextTrack as MediaTrack.Track)
                        return false
                    }
                    else {
                        session.queue.subIndex = 0
                        availableTracks.removeAt(nextIndex)
                    }
                }
                is MediaTrack.BaseTrack -> throw UnknownFormatFlagsException("${nextMedia::class.simpleName} is illegal class")
            }
        }
        return true
    }
}