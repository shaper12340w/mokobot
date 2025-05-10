package dev.shaper.rypolixy.core.music

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
                    logger.debug { "Track End Up With Reason : ${event.endReason}" }
                    when (event.endReason) {
                        AudioTrackEndReason.CLEANUP     -> {
                            if(session.player.status == MediaOptions.PlayerStatus.PAUSED){
                                logger.warn { "Track is terminated! "}
                                session.player.status = MediaOptions.PlayerStatus.TERMINATED
                            }
                        }
                        AudioTrackEndReason.FINISHED    -> {
                            if(session.player.status != MediaOptions.PlayerStatus.ERROR) {
                                session.currentTrack().data.status = MediaBehavior.PlayStatus.END
                                session.queue.position = 0
                                player.next(guildId)
                            } else {
                                try {
                                    session.currentTrack().data.status = MediaBehavior.PlayStatus.IDLE
                                    session.queue.position  = player.getPosition(guildId)
                                    RetryUtil.retry { session.reload() }
                                } catch(e:Exception){
                                    player.sendError(guildId,e)
                                    player.next(guildId)
                                }
                            }
                        }
                        AudioTrackEndReason.LOAD_FAILED -> {
                            logger.error { "Track load failed" }
                            player.sendError(guildId,Exception("Track load failed"))
                        }
                        else -> Unit
                    }
                }
                is TrackStartEvent -> {
                    session.options.volume = player.getVolume(guildId)
                } //Todo : Add stack or deque to preload others
                is TrackExceptionEvent -> {
                    session.player.status = MediaOptions.PlayerStatus.ERROR
                    try { session.reload() }
                    catch (ex:Exception) { throw ex }
                }
                is PlayerPauseEvent -> {
                    session.queue.position    = player.getPosition(guildId)
                    session.player.status     = MediaOptions.PlayerStatus.PAUSED
                }
                is PlayerResumeEvent -> {
                    if(session.player.status == MediaOptions.PlayerStatus.TERMINATED){
                        try { session.update() } //Try reload from saved url
                        catch (e:Exception){
                            try { session.reload() }  //Try re-search and reload
                            catch (ex:Exception) { throw ex } //else fuc stop it
                        }
                    }
                    session.player.status = MediaOptions.PlayerStatus.PLAYING
                }
            }
        }
    }
}