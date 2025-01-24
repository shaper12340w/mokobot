package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType
import dev.shaper.rypolixy.utils.musicplayer.MediaTrack
import dev.shaper.rypolixy.utils.musicplayer.MediaType
import dev.shaper.rypolixy.utils.musicplayer.lavaplayer.LookupResult


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

        val searchedTrack: MediaType.SearchResult? = when(context){
            is ContextType.Message -> {
                if(res?.command == null){
                    context.sendRespond(ResponseType.NORMAL,EmbedFrame.error("검색어를 입력해주세요",null))
                    null
                }
                else{
                    fun findKeyForInput(optionMap: HashMap<MediaType.MediaSource, List<String>>, input: String): MediaType.MediaSource? {
                        for ((key, values) in optionMap) {
                            if (input in values) {
                                return key
                            }
                        }
                        return null
                    }
                    val optionMap = HashMap<MediaType.MediaSource,List<String>>()
                    optionMap.apply{
                        put(MediaType.MediaSource.YOUTUBE, listOf("Y", "youtube", "yt"))
                        put(MediaType.MediaSource.SOUNDCLOUD, listOf("sc", "soundcloud", "S"))
                        put(MediaType.MediaSource.SPOTIFY, listOf("sp", "spotify"))
                        put(MediaType.MediaSource.UNKNOWN, listOf("url","U","uri"))
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
                    "youtube"       -> MediaType.MediaSource.YOUTUBE
                    "soundcloud"    -> MediaType.MediaSource.SOUNDCLOUD
                    "spotify"       -> MediaType.MediaSource.SPOTIFY
                    "url"           -> MediaType.MediaSource.UNKNOWN
                    else            -> null
                }
                if(platform!=null)
                    client.lavaClient.search(command.strings["search"]!!, platform)
                else
                    client.lavaClient.search(command.strings["search"]!!)

            }
        }
        if(searchedTrack != null) {
            when(searchedTrack.result){
                is LookupResult.Success -> {
                    when(searchedTrack.data){
                        is MediaTrack.Track -> {
                            client.lavaClient.play(listOf(searchedTrack.data),context.guildId)
                            context.sendRespond(ResponseType.NORMAL, EmbedFrame.musicInfo(searchedTrack.data,false))
                        }
                        is MediaTrack.Playlist -> {
                            val test = searchedTrack.data.tracks[1]
                            client.lavaClient.play(listOf(test),context.guildId)
                            context.sendRespond(ResponseType.NORMAL, EmbedFrame.musicInfo(test,false))
                        }
                        else -> context.sendRespond(ResponseType.NORMAL,EmbedFrame.warning("검색 결과가 없습니다",null))

                    }

                }
                is LookupResult.Error       -> context.sendRespond(ResponseType.NORMAL,EmbedFrame.error("에러가 발생했습니다",null))
                is LookupResult.NoResults   -> context.sendRespond(ResponseType.NORMAL,EmbedFrame.warning("검색 결과가 없습니다",null))
            }
        }
        else
            context.sendRespond(ResponseType.NORMAL,EmbedFrame.warning("검색 결과가 없습니다",null))

    }

}