package dev.shaper.rypolixy.config.database.builder

import java.sql.Connection
import java.sql.PreparedStatement
import io.github.oshai.kotlinlogging.KotlinLogging

class Query{

    private val text = StringBuilder()
    private val logger = KotlinLogging.logger {}

    fun column(name: String, type: String, constraints: String = "") {
        if (text.isNotEmpty()) {
            text.append(",\n")
        }
        text.append("$name $type $constraints")
    }



    override fun toString(): String {
        return "$text"
    }

}