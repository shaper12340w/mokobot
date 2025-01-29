package dev.shaper.rypolixy.utils.cmdflow

import com.github.ajalt.clikt.core.CliktCommand

class OptionCommandBuilder {
    private val commands = mutableListOf<CliktCommand>()

    fun command(name: String, action: OptionCommandScope.() -> Unit) {
        commands.add(OptionCommandScope(name).apply(action).build())
    }

    fun build(): List<CliktCommand> = commands

    companion object {
        fun createCommands(block: OptionCommandBuilder.() -> Unit): List<CliktCommand> {
            return OptionCommandBuilder().apply(block).build()
        }
    }
}