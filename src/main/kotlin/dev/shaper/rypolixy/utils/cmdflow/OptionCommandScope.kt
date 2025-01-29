package dev.shaper.rypolixy.utils.cmdflow

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.OptionDelegate
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.multiple

class OptionCommandScope(private val name: String) {
    private val options = mutableListOf<Pair<String, OptionDelegate<*>>>()
    private var runAction: OptionCommandAction? = null

    fun option(name: String, help: String? = null): OptionDelegate<String?> {
        val delegate = option(name, help)
        options.add(name to delegate)
        return delegate
    }

    fun multipleOption(name: String, help: String? = null): OptionWithValues<List<String>, String, String> {
        val baseOption = option(name, help)
        if (baseOption !is NullableOption<*, *>) {
            throw IllegalStateException("Option must be NullableOption to use multiple")
        }
        @Suppress("UNCHECKED_CAST")
        val nullableOption = baseOption as NullableOption<String, String>
        val multiOption = nullableOption.multiple()
        options.add(name to multiOption)
        return multiOption
    }

    fun run(action: OptionCommandAction) {
        runAction = action
    }

    fun build(): CliktCommand {
        return object : CliktCommand(name = name) {
            override fun run() {
                runAction?.execute()
            }
        }
    }
}
