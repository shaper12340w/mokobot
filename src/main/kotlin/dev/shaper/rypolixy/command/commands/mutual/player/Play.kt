package dev.shaper.rypolixy.command.commands.mutual.player

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.MessageBuilder
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.core.musicplayer.MediaOptions
import dev.shaper.rypolixy.utils.discord.CommandCaller
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.sendRespond
import dev.shaper.rypolixy.utils.discord.context.ResponseType
import dev.shaper.rypolixy.utils.discord.context.ReturnType
import dev.shaper.rypolixy.core.musicplayer.MediaTrack
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import dev.shaper.rypolixy.utils.discord.KordUtils.avatarUrl
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.getUser
import dev.shaper.rypolixy.utils.discord.context.DefaultMessageBuilder
import dev.shaper.rypolixy.utils.discord.embed.PageEmbed
import dev.shaper.rypolixy.utils.io.database.DatabaseManager
import us.jimschubert.kopper.Parser


class Play(private val client: Client): MutualCommand {

    override val name           : String                    = "play"
    override val description    : String                    = "Play a song"
    override val enabled        : Boolean                   = true
    override val isInteractive  : Boolean                   = true
    override val commandType    : TextCommand.CommandType   = TextCommand.CommandType()

    override fun setup(builder: ChatInputCreateBuilder) {
        builder.apply {
            defaultMemberPermissions = Permissions(Permission.SendMessages)
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

    override fun setup(builder: Parser) {
        builder.apply {
            option("yt",listOf("youtube"),"search youtube")
            option("sc", listOf("soundcloud"),"search soundcloud")
            option("sp",listOf("spotify"),"search spotify")
            option("url",listOf("url","uri"),"search url")
        }
    }

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        val findPlayer = client.lavaClient.sessions[context.guildId]
        val waitMessage = context.sendRespond(
            ResponseType.NORMAL,
            EmbedFrame.loading("로딩중입니다. 잠시만 기다려 주세요",null).apply { footer = EmbedBuilder.Footer().apply { text = "유튜브의 경우 시간이 다소 소요될 수 있습니다"} })

        suspend fun respond(message: MessageBuilder.() -> Unit): Message? {
            when(waitMessage){
                is ReturnType.Message        -> { return waitMessage.data.edit (message) }
                is ReturnType.Interaction    -> { return (waitMessage.data as PublicMessageInteractionResponseBehavior).edit (message).message }
                else -> return null
            }
        }
        if(findPlayer == null)
            CommandCaller.call(client,"join",context,"silent")

        val defaultPlatform = DatabaseManager.getGuildData(context.guildId).playerData.platform
        val searchedTrack: MediaOptions.SearchResult? = when(context){
            is ContextType.Message -> {
                if(res?.command == null){
                    respond { embeds = mutableListOf(EmbedFrame.error("검색어를 입력해주세요",null)) }
                    null
                }
                else{
                    when {
                        res.options.option("yt") != null          -> client.lavaClient.search(res.options.option("yt")!!,
                            MediaUtils.MediaPlatform.YOUTUBE)
                        res.options.option("sc") != null          -> client.lavaClient.search(res.options.option("sc")!!,
                            MediaUtils.MediaPlatform.SOUNDCLOUD)
                        res.options.option("sp") != null          -> client.lavaClient.search(res.options.option("sp")!!,
                            MediaUtils.MediaPlatform.SPOTIFY)
                        res.options.option("url") != null         -> client.lavaClient.search(res.options.option("url")!!,
                            MediaUtils.MediaPlatform.UNKNOWN)
                        res.command.isNotBlank()                        -> client.lavaClient.search(res.command, defaultPlatform)
                        res.options.unparsedArgs.isNotEmpty()           -> {
                            respond { embeds = mutableListOf(EmbedFrame.error("잘못된 사용법입니다",null)) }
                            null
                        }
                        else -> {
                            respond { embeds = mutableListOf(EmbedFrame.error("잘못된 사용법입니다",null)) }
                            null
                        }
                    }
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
                    client.lavaClient.search(command.strings["search"]!!,defaultPlatform)

            }
        }
        if(searchedTrack != null) {

            when(searchedTrack.status){
                is MediaOptions.SearchType.SUCCESS -> {
                    val image = context.getUser().avatarUrl()
                    when(searchedTrack.data){
                        is MediaTrack.Track -> {
                            client.lavaClient.play(searchedTrack.data,context.guildId)
                            respond { embeds = mutableListOf(EmbedFrame.musicInfo(searchedTrack.data,image)) }
                        }
                        is MediaTrack.Playlist -> {
                            if(searchedTrack.data.isSeek){
                                if(searchedTrack.data.tracks.isNotEmpty()){
                                    val test = searchedTrack.data.tracks[0]
                                    val track = client.lavaClient.play(test,context.guildId)
                                    respond { embeds = mutableListOf(EmbedFrame.musicInfo(track!!,image)) }
                                } else {
                                    respond { embeds = mutableListOf(EmbedFrame.warning("검색 결과가 없습니다",null)) }
                                }
                            }
                            else{
                                client.lavaClient.play(searchedTrack.data,context.guildId)
                                val pageBuilder = PageEmbed(searchedTrack.data.title)
                                    .autoSplitEmbed(searchedTrack.data.tracks.joinToString("\n") { "[${it.title}](${it.url})" })
                                val page = pageBuilder.build()
                                val response = respond {
                                    content     = "``재생목록을 재생합니다``"
                                    embeds      = page.embeds
                                    components  = page.components
                                }
                                pageBuilder.setMessage(response!!)
                            }
                        }
                        else -> context.sendRespond(ResponseType.NORMAL, EmbedFrame.warning("검색 결과가 없습니다",null))

                    }

                }
                is MediaOptions.SearchType.ERROR       -> respond { embeds = mutableListOf(EmbedFrame.error("에러가 발생했습니다",searchedTrack.status.exception.message)) }
                is MediaOptions.SearchType.NORESULTS   -> respond { embeds = mutableListOf(EmbedFrame.warning("검색 결과가 없습니다",null)) }
            }


        }
        else
            context.sendRespond(ResponseType.NORMAL, EmbedFrame.warning("검색 결과가 없습니다",null))

    }

}