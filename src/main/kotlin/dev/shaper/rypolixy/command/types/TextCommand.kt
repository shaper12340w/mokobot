package dev.shaper.rypolixy.command.types

import dev.kord.core.event.message.MessageCreateEvent

interface TextCommand: CommandStructure {

    companion object{
        const val commonPrefix:String = "!"
    }

    val commandType: CommandType


    data class CommandType(
        val prefix:MutableList<String>?,
        val suffix:MutableList<String>?,
        val equals:MutableList<String>?
    )

    data class ResponseData(
        val keyword     :String?,
        val command     :String?,
        val options     :List<String>?
    )

    suspend fun execute(event: MessageCreateEvent,res: ResponseData?)

}