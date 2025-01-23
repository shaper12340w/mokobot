package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.Colors
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.interaction
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType
import dev.shaper.rypolixy.utils.musicplayer.AudioPlayer
import dev.shaper.rypolixy.utils.musicplayer.AudioTrack


class Play(private val client: Client): MutualCommand {

    override val name       : String
        get()          = "play"

    override val description: String
        get()          = "play a song"

    override val commandType: TextCommand.CommandType
        get()          = TextCommand.CommandType(prefix = null, suffix = null, equals = null)

    override val enabled    : Boolean
        get() = true

    override val isInteractive: Boolean
        get() = true

    override fun setup(builder: ChatInputCreateBuilder) {
        builder.apply {
            defaultMemberPermissions = Permissions(Permission.ManageMessages)
            string("search","video or music url or to search"){
                required = true
            }
            string("platform","choose platform"){
                required = false
                choice("youtube","youtube")
                choice("soundcloud","soundcloud")
                choice("spotify","spotify")
                choice("url","url")
            }

        }
    }

    @OptIn(KordVoice::class)
    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        val findPlayer = client.lavaClient.sessions[context.guildId]
        if(findPlayer == null){
            client.commandManager.mutualCommand["join"]!!.execute(context, TextCommand.ResponseData("join",null,listOf("silence")))
            logger.info { "Called Joined command" }
        }

        val searchedTrack: List<AudioTrack>? = when(context){
            is ContextType.Message -> {
                if(res?.command == null){
                    context.sendRespond(ResponseType.NORMAL,EmbedFrame.error("검색어를 입력해주세요",null))
                    null
                }
                else{
                    fun findKeyForInput(optionMap: HashMap<AudioPlayer.SearchOptions, List<String>>, input: String): AudioPlayer.SearchOptions? {
                        for ((key, values) in optionMap) {
                            if (input in values) {
                                return key
                            }
                        }
                        return null
                    }
                    val optionMap = HashMap<AudioPlayer.SearchOptions,List<String>>()
                    optionMap.apply{
                        put(AudioPlayer.SearchOptions.YOUTUBE, listOf("Y", "youtube", "yt"))
                        put(AudioPlayer.SearchOptions.SOUNDCLOUD, listOf("sc", "soundcloud", "S"))
                        put(AudioPlayer.SearchOptions.SPOTIFY, listOf("sp", "spotify"))
                        put(AudioPlayer.SearchOptions.URL, listOf("url","U","uri"))
                    }


                    if(res.options != null){
                        val platform = findKeyForInput(optionMap, res.options[0])
                        if (platform!=null){
                            client.lavaClient.search(res.command, platform)
                        }
                        context.sendRespond(ResponseType.NORMAL,EmbedFrame.error("잘못된 플랫폼입니다",null))
                        null
                    }
                    else
                        client.lavaClient.search(res.command)
                }
            }
            is ContextType.Interaction -> {
                val command = context.value.interaction.command
                val platform = when(command.strings["platform"]){
                    "youtube"       -> AudioPlayer.SearchOptions.YOUTUBE
                    "soundcloud"    -> AudioPlayer.SearchOptions.SOUNDCLOUD
                    "spotify"       -> AudioPlayer.SearchOptions.SPOTIFY
                    "url"           -> AudioPlayer.SearchOptions.URL
                    else            -> null
                }
                if(platform!=null)
                    client.lavaClient.search(command.strings["search"]!!, platform)
                else
                    client.lavaClient.search(command.strings["search"]!!)

            }
        }
        if(!searchedTrack.isNullOrEmpty()) {
            client.lavaClient.play(searchedTrack,context.guildId)
            context.sendRespond(ResponseType.NORMAL, EmbedFrame.musicInfo(searchedTrack[0],false))
        }
        else
            context.sendRespond(ResponseType.NORMAL,EmbedFrame.warning("검색 결과가 없습니다",null))

    }

}