package dev.shaper.rypolixy.command.commands.mutual

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.rest.Image
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.route.CdnUrl
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.logger
import dev.shaper.rypolixy.utils.cmdflow.OptionCommandBuilder
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.getMember
import dev.shaper.rypolixy.utils.discord.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.EmbedFrame
import dev.shaper.rypolixy.utils.discord.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.ResponseType
import dev.shaper.rypolixy.utils.discord.ReturnType
import dev.shaper.rypolixy.utils.musicplayer.MediaTrack
import dev.shaper.rypolixy.utils.musicplayer.MediaUtils


class Play(private val client: Client): MutualCommand {

    override val name           : String                    = "play"
    override val description    : String                    = "Play a song"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

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

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        val findPlayer = client.lavaClient.sessions[context.guildId]
        val waitMessage = context.sendRespond(ResponseType.NORMAL,EmbedFrame.loading("로딩중입니다. 잠시만 기다려 주세요",null).apply { footer = EmbedBuilder.Footer().apply { text = "유튜브의 경우 시간이 다소 소요될 수 있습니다"} })
        suspend fun respond(emb: EmbedBuilder){
            when(waitMessage){
                is ReturnType.Message        -> { waitMessage.data.edit { embeds = listOf(emb).toMutableList() } }
                is ReturnType.Interaction    -> { (waitMessage.data as PublicMessageInteractionResponseBehavior).edit { embeds = listOf(emb).toMutableList() } }
                else -> Unit
            }

        }
        if(findPlayer == null){
            client.commandManager.mutualCommand["join"]!!.execute(context, TextCommand.ResponseData("join",null,listOf("silence")))
            logger.info { "Called Joined command" }
        }

        val searchedTrack: MediaUtils.SearchResult? = when(context){
            is ContextType.Message -> {
                if(res?.command == null){
                    respond(EmbedFrame.error("검색어를 입력해주세요",null))
                    null
                }
                else{
                    fun findKeyForInput(optionMap: HashMap<MediaUtils.MediaPlatform, List<String>>, input: String): MediaUtils.MediaPlatform? {
                        for ((key, values) in optionMap) {
                            if (input in values) {
                                return key
                            }
                        }
                        return null
                    }
                    val optionMap = HashMap<MediaUtils.MediaPlatform,List<String>>()
                    optionMap.apply{
                        put(MediaUtils.MediaPlatform.YOUTUBE, listOf("Y", "youtube", "yt"))
                        put(MediaUtils.MediaPlatform.SOUNDCLOUD, listOf("sc", "soundcloud", "S"))
                        put(MediaUtils.MediaPlatform.SPOTIFY, listOf("sp", "spotify"))
                        put(MediaUtils.MediaPlatform.UNKNOWN, listOf("url","U","uri"))
                    }


                    if(res.options != null){
                        val platform = findKeyForInput(optionMap, res.options[0])
                        if (platform!=null){
                            client.lavaClient.search(res.command, platform)
                        } else {
                            respond(EmbedFrame.error("잘못된 플랫폼입니다",null))
                            null
                        }
                    }
                    else
                        client.lavaClient.search(res.command)
                }
            }
            is ContextType.Interaction -> {
                val command     = context.value.interaction.command
                val platform    = when(command.strings["platform"]){
                    "youtube"       -> MediaUtils.MediaPlatform.YOUTUBE
                    "soundcloud"    -> MediaUtils.MediaPlatform.SOUNDCLOUD
                    "spotify"       -> MediaUtils.MediaPlatform.SPOTIFY
                    "url"           -> MediaUtils.MediaPlatform.UNKNOWN
                    else            -> null
                }
                if(platform!=null)
                    client.lavaClient.search(command.strings["search"]!!, platform)
                else
                    client.lavaClient.search(command.strings["search"]!!)

            }
        }
        if(searchedTrack != null) {

            when(searchedTrack.status){
                MediaUtils.SearchType.SUCCESS -> {
                    val image = context.getMember().avatar?.cdnUrl?.toUrl {
                        CdnUrl.UrlFormatBuilder().apply {
                            format = Image.Format.WEBP
                            size   = Image.Size.Size1024
                        }
                    }
                    when(searchedTrack.data){
                        is MediaTrack.Track -> {
                            client.lavaClient.play(searchedTrack.data,context.guildId)
                            respond(EmbedFrame.musicInfo(searchedTrack.data,image))
                        }
                        is MediaTrack.Playlist -> {
                            if(searchedTrack.data.isSeek){
                                val test = searchedTrack.data.tracks[0]
                                val track = client.lavaClient.play(test,context.guildId)
                                respond(EmbedFrame.musicInfo(track!!,image))
                            }
                            else{
                                client.lavaClient.play(searchedTrack.data,context.guildId)
                                respond(EmbedFrame.list(searchedTrack.data.title,
                                    searchedTrack.data.tracks.joinToString("\n") { it.title }) {
                                    thumbnail { url = searchedTrack.data.thumbnail ?: "" }
                                })
                            }
                        }
                        else -> context.sendRespond(ResponseType.NORMAL,EmbedFrame.warning("검색 결과가 없습니다",null))

                    }

                }
                MediaUtils.SearchType.ERROR       -> respond(EmbedFrame.error("에러가 발생했습니다",null))
                MediaUtils.SearchType.NORESULTS   -> respond(EmbedFrame.warning("검색 결과가 없습니다",null))
            }


        }
        else
            context.sendRespond(ResponseType.NORMAL,EmbedFrame.warning("검색 결과가 없습니다",null))

    }

}