package dev.shaper.rypolixy.core.musicplayer

import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.kord.common.entity.Snowflake
import dev.shaper.rypolixy.utils.structure.RetryUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaEvent(
    private val player: MediaPlayer,
    private val guildId:Snowflake
): AudioEventListener {

    private val logger = KotlinLogging.logger {}

    override fun onEvent(event: AudioEvent?) {

        val session = player.sessions[guildId] ?: return

        CoroutineScope(Dispatchers.IO).launch {

            when (event) {
                is TrackStuckEvent  -> logger.warn { "Track is Stuck" }
                is TrackEndEvent    -> {
                    logger.debug { event.endReason }
                    if(event.endReason == AudioTrackEndReason.CLEANUP && session.options.paused){
                        logger.warn { "Track is terminated! "}
                        session.options.terminated = true
                    }
                    else{
                        session.currentTrack()?.data?.status = MediaBehavior.PlayStatus.END
                        player.playNext(guildId)
                    }
                }
                is TrackStartEvent -> {
                    session.player.volume = player.getVolume(guildId)
                } //Todo : Add stack or deque to preload others
                is TrackExceptionEvent -> {
                    try {
                        RetryUtil.retry { session.reload() }
                    } catch(e:Exception){
                        player.sendError(guildId,event.exception)
                        player.playNext(guildId)
                    }
                }
                is PlayerPauseEvent -> {
                    session.options.position    = session.currentTrack()?.data?.audioTrack?.position ?: 0
                    session.options.paused      = true
                }
                is PlayerResumeEvent -> {
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
}