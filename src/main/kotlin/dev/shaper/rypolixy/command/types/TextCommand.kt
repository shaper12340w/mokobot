package dev.shaper.rypolixy.command.types

import dev.kord.core.event.message.MessageCreateEvent
import us.jimschubert.kopper.ArgumentCollection
import us.jimschubert.kopper.Parser

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
        val command     :String?,
        val options     :ArgumentCollection
    )

    fun setup(builder: Parser) {}

    suspend fun execute(event: MessageCreateEvent,res: ResponseData?)

}