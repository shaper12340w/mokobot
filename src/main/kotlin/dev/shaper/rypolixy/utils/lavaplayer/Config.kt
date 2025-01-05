package dev.shaper.rypolixy.utils.lavaplayer

import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Guild

data class Config(
    val guild: Guild,
    val channel: MessageChannelBehavior,
    val voiceChannel: BaseVoiceChannelBehavior?,
    val startVolume: Int,
    val botAloneTime: Int
)
