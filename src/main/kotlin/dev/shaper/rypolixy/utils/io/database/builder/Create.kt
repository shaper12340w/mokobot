package dev.shaper.rypolixy.utils.io.database.builder

class Create: SqlBuilder() {

    private var name:String = ""
    var exist:Boolean  = false

    fun create(tableName:String, init: Query.() -> Unit) = apply{
        name = tableName
        elements.add(Query().apply(init))
    }

    fun isNotExist(bool:Boolean){
        exist = bool
    }

    override fun toString(): String {
        val elementsString = elements.joinToString(separator = ",\n") { it.toString() }
        return "CREATE TABLE ${ if (exist) "IF NOT EXISTS" else "" } $name ($elementsString)"
    }
}