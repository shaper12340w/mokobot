package dev.shaper.rypolixy.config.database.builder

class Insert:SqlBuilder() {

    private var columns     = mutableListOf<String>()
    private var name:String = ""

    fun insert(tableName:String,init:Query.() -> Unit){
        name = tableName
        elements.add(Query().apply(init))
    }

    fun setColumn(vararg column:String){
        columns.addAll(column)
    }

    override fun toString(): String {
        val elementsString = elements.joinToString(separator = ",\n") { it.toString() }
        return "INSERT INTO $name${if (columns.isNotEmpty()) " (${columns.joinToString { ", " }})" else ""} VALUES ($elementsString)"
    }


}