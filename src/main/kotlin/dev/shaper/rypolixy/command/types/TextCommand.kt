package dev.shaper.rypolixy.command.types

import dev.kord.core.event.message.MessageCreateEvent
import dev.shaper.rypolixy.utils.cmdflow.OptionCommandBuilder

interface TextCommand: CommandStructure {

    companion object{
        const val commonPrefix:String = "!"
    }

    val commandType: CommandType


    data class CommandType(
        val prefix:MutableList<String>? = null,
        val suffix:MutableList<String>? = null,
        val equals:MutableList<String>? = null,
    )

    data class ResponseData(
        val keyword     :String?,
        val command     :String?,
        val options     :List<String>?
    )

    fun setup(builder: OptionCommandBuilder) {}

    suspend fun execute(event: MessageCreateEvent,res: ResponseData?)

}