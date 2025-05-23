package dev.shaper.rypolixy.core.music

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.shaper.rypolixy.core.music.MediaPlayer.Companion.logger
import dev.shaper.rypolixy.core.music.parser.MediaParser
import dev.shaper.rypolixy.core.music.utils.MediaUtils
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import dev.shaper.rypolixy.utils.structure.RetryUtil
import java.util.*

class MediaTrackHandler(private val player: MediaPlayer, private val guildId: Snowflake) {

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
        session.player.status = MediaOptions.PlayerStatus.IDLE
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
        try {
            if(handleNextTrack()){
                resetSession()
                return null
            }
            else return session.currentTrack()
        } catch (e:Exception) {
            player.sendError(guildId,e)
            RetryUtil.retry { handleNextTrack() }
            return null
        }

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

        if(session.options.shuffle)
            availableTracks.shuffle()
        availableTracks.addAll(currentTracks)

        while (availableTracks.isNotEmpty()) {
            val nextIndex = if (session.queue.subIndex > 0)
                session.queue.index
            else 0
            val nextMedia = availableTracks[nextIndex]
            fun getIndex(): Int {
                val index = currentTracks.indexOf(nextMedia)
                if(index == -1)
                    throw IndexOutOfBoundsException("Track not found")
                return index
            }
            when (nextMedia) {
                is MediaTrack.Track -> {
                    if(nextMedia.data.status == MediaBehavior.PlayStatus.IDLE) { //Check Track is Played
                        session.queue.index = getIndex()
                        playTrack(nextMedia)
                        return false
                    }
                    else availableTracks.removeAt(nextIndex)
                }
                is MediaTrack.FlatTrack -> {
                    val convertedTrack  = nextMedia.toTrack()
                    val trackIndex      = getIndex()
                    if (convertedTrack != null) {
                        session.queue.tracks[trackIndex] = convertedTrack
                        session.queue.index = trackIndex
                        playTrack(convertedTrack)
                    }
                    else availableTracks.removeAt(nextIndex)
                    return false
                }
                is MediaTrack.Playlist -> {
                    val idleTracks = nextMedia.tracks.filterIsInstance<MediaTrack.FlatTrack>() // Collect FlatTracks (Not Implemented)
                        .plus(nextMedia.tracks.filterIsInstance<MediaTrack.Track>()
                            .filter { it.data.status == MediaBehavior.PlayStatus.IDLE }) // Collect Track Not Played
                    if (idleTracks.isNotEmpty()) {
                        session.queue.index = getIndex()
                        val currentPlaylist = currentTracks[session.queue.index] as MediaTrack.Playlist
                        val nextTrack = if(session.options.shuffle) idleTracks.shuffled().first() else idleTracks.first()
                        var implementedTrack: MediaTrack.Track? = null
                        if(nextTrack is MediaTrack.FlatTrack){
                            val currentSubIndex = currentPlaylist.tracks.indexOf(nextTrack)
                            val convertedTrack  = nextTrack.toTrack()
                            if(convertedTrack != null) {
                                implementedTrack = convertedTrack
                                currentPlaylist.tracks[currentSubIndex] = implementedTrack
                                session.queue.subIndex = currentSubIndex
                            }
                            else {
                                currentPlaylist.tracks.remove(nextTrack)
                                return false
                            }
                        }
                        else session.queue.subIndex = currentPlaylist.tracks.indexOf(nextTrack)
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