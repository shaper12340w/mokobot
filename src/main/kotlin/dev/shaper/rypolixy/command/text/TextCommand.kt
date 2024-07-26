package dev.shaper.rypolixy.command.text

import dev.shaper.rypolixy.command.CommandStructure
import dev.kord.core.event.message.MessageCreateEvent

abstract class TextCommand: CommandStructure() {

    val commonPrefix:String = "!"

    abstract val commandType: CommandType


    data class CommandType(
        val prefix:MutableList<String>?,
        val suffix:MutableList<String>?,
        val equals:MutableList<String>?
    )

    data class ResponseData(
        val keyword     :String?,
        val commandValue:String?
    )

    abstract suspend fun execute(event: MessageCreateEvent,res:ResponseData?)

}