package dev.shaper.rypolixy.command.commands.mutual.player

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.response.InteractionResponseBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.shaper.rypolixy.command.types.ContextType
import dev.shaper.rypolixy.command.types.MutualCommand
import dev.shaper.rypolixy.command.types.TextCommand
import dev.shaper.rypolixy.config.Client
import dev.shaper.rypolixy.core.musicplayer.MediaOptions
import dev.shaper.rypolixy.core.musicplayer.MediaTrack
import dev.shaper.rypolixy.core.musicplayer.utils.MediaUtils
import dev.shaper.rypolixy.utils.discord.CommandCaller
import dev.shaper.rypolixy.utils.discord.actionrow.ActionRowManager
import dev.shaper.rypolixy.utils.discord.actionrow.DefaultSelectMenu
import dev.shaper.rypolixy.utils.discord.actionrow.SelectMenuData
import dev.shaper.rypolixy.utils.discord.actionrow.SelectMenuType
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.channel
import dev.shaper.rypolixy.utils.discord.context.ContextManager.Companion.guildId
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.createDefer
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.deferReply
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.delete
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.deleteAfter
import dev.shaper.rypolixy.utils.discord.context.ResponseManager.Companion.getMessage
import dev.shaper.rypolixy.utils.discord.context.ResponseType
import dev.shaper.rypolixy.utils.discord.context.ReturnType
import dev.shaper.rypolixy.utils.discord.embed.EmbedFrame
import dev.shaper.rypolixy.utils.io.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.jimschubert.kopper.Parser
import java.util.*

class Search(private val client: Client): MutualCommand {
    override val name           : String                    = "search"
    override val description    : String                    = "Search songs"
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
            }

        }
    }

    override fun setup(builder: Parser) {
        builder.apply {
            option("yt",listOf("youtube"),"search youtube")
            option("sc", listOf("soundcloud"),"search soundcloud")
            option("sp",listOf("spotify"),"search spotify")
        }
    }

    override suspend fun execute(context: ContextType, res: TextCommand.ResponseData?) {
        val session         = client.lavaClient.sessions[context.guildId] ?: CommandCaller.call(client,"join",context,"silent")
        val deferResponse   = context.createDefer(ResponseType.EPHEMERAL)
        val defaultPlatform = DatabaseManager.getGuildData(context.guildId).playerData.platform
        val platformMap: MutableMap<String,MediaUtils.MediaPlatform>
                = mutableMapOf<String,MediaUtils.MediaPlatform>().apply {
            MediaUtils.MediaPlatform.entries.forEach { platform ->
                put(platform::class.simpleName!!,platform)
            }
        }
        var searchedData    : MediaOptions.SearchResult? = null

        when (context) {
            is ContextType.Message -> {
                if(res?.command == null)
                    deferResponse.deferReply { embeds = mutableListOf(EmbedFrame.error("검색어를 입력해주세요",null)) }
                else {

                    if(res.options.unparsedArgs.isNotEmpty())
                        deferResponse.deferReply { embeds = mutableListOf(EmbedFrame.error("잘못된 사용법입니다",null)) }
                    else {
                        val checkPlatform = platformMap.keys.find { platform -> res.options.option(platform) != null }
                        if(checkPlatform != null)
                            CoroutineScope(Dispatchers.IO).launch{ searchedData = client.lavaClient.search(res.options.option(checkPlatform)!!, platformMap[checkPlatform]!!) }
                        else
                            searchedData = client.lavaClient.search(res.command,defaultPlatform)
                    }
                }
            }
            is ContextType.Interaction -> {
                val command     = context.value.interaction.command
                val checkPlatform = platformMap.keys.find { platform -> platform == command.strings["platform"] }
                searchedData = if(checkPlatform != null)
                    client.lavaClient.search(command.strings["search"]!!,platformMap[checkPlatform]!!)
                else
                    client.lavaClient.search(command.strings["search"]!!,defaultPlatform)
            }
        }
        if (searchedData != null) {
            when(searchedData!!.status){
                is MediaOptions.SearchType.SUCCESS -> {
                    when(searchedData!!.data){
                        is MediaTrack.Track -> {
                            deferResponse.deferReply { embeds = mutableListOf(EmbedFrame.info("검색 결과가 하나이므로 자동으로 추가합니다",null)) }
                                .deleteAfter(3000)
                            client.lavaClient.play(searchedData!!.data!!,context.guildId)
                            context.channel.createMessage { embeds = mutableListOf(EmbedFrame.musicInfo(searchedData!!.data!! as MediaTrack.Track)) }
                        }
                        is MediaTrack.Playlist -> {
                            val playlist = searchedData!!.data!! as MediaTrack.Playlist
                            if (playlist.isSeek){
                                if(playlist.tracks.isNotEmpty()){
                                    var selectMessage: ReturnType? = null
                                    suspend fun selectMenuExecute(interaction: SelectMenuInteraction, kill: () -> Unit){
                                        interaction.deferEphemeralMessageUpdate()
                                        val track = client.lavaClient.play(playlist.tracks[interaction.values.first().toInt()],context.guildId)
                                        context.channel.createMessage { embeds = mutableListOf(EmbedFrame.musicInfo(track!!)) }
                                        selectMessage?.delete()
                                        kill()
                                    }
                                    val optionList = mutableListOf<SelectOptionBuilder>()
                                        .apply { playlist.tracks.forEachIndexed{ index,track -> add(SelectOptionBuilder(track.title,index.toString()).apply { description = track.artist })} }
                                    val selectMenu = ActionRowManager.CreateSelectMenu(
                                        UUID.randomUUID(),
                                        SelectMenuData(
                                            listOf(DefaultSelectMenu(
                                                type            = SelectMenuType.STRING,
                                                options         = optionList,
                                                executeFunction = ::selectMenuExecute
                                            ))
                                        )
                                    ).build()
                                    selectMessage = deferResponse.deferReply {
                                        embeds      = mutableListOf(EmbedFrame.list("재생할 곡을 골라주세요",playlist.tracks.joinToString("\n") { it.title }))
                                        components  = mutableListOf(selectMenu)
                                    }
                                }
                            }
                        }
                        else -> deferResponse.deferReply { embeds = mutableListOf(EmbedFrame.warning("검색 결과가 없습니다",null)) }
                    }
                }
                is MediaOptions.SearchType.NORESULTS    -> deferResponse.deferReply { embeds = mutableListOf(EmbedFrame.warning("검색 결과가 없습니다",null)) }.deleteAfter(3000)
                is MediaOptions.SearchType.ERROR        -> deferResponse.deferReply { embeds = mutableListOf(EmbedFrame.error("에러가 발생했습니다",(searchedData!!.status as MediaOptions.SearchType.ERROR).exception.message)) }.deleteAfter(3000)
            }
        }
    }
}