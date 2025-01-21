package dev.shaper.rypolixy.utils.io.database.builder

import java.sql.PreparedStatement

@Deprecated("This class is deprecated. Use Query directly.")
abstract class SqlBuilder {
    protected val elements = mutableListOf<Any>()

    override fun toString(): String {
        return elements.joinToString(separator = "")
    }
}

