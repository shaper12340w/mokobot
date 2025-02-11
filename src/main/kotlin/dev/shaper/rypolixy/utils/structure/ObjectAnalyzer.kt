package dev.shaper.rypolixy.utils.structure

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

// Object 기반 분석기
object ObjectAnalyzer {
    private val visited = mutableSetOf<Int>() // 순환 참조 방지용 Set

    // 객체를 재귀적으로 분석
    private fun analyze(obj: Any?, depth: Int = 0, maxDepth: Int = 5): String {
        if (obj == null) return "null"

        // 깊이 제한
        if (depth > maxDepth) return "...(too deep)"

        val objHash = System.identityHashCode(obj)
        if (!visited.add(objHash)) return "...(circular reference)" // 순환 참조 방지

        val indent = "  ".repeat(depth)
        val kClass = obj::class
        val builder = StringBuilder()

        builder.append("$indent${kClass.simpleName} {\n")

        val properties = kClass.memberProperties
        for (property in properties) {
            try {
                val value = (property as KProperty1<Any, *>).get(obj)
                if (value == null || isPrimitive(value)) {
                    builder.append("$indent  ${property.name} = $value\n")
                } else {
                    builder.append("$indent  ${property.name} =\n${analyze(value, depth + 1, maxDepth)}")
                }
            } catch (e: Exception) {
                builder.append("$indent  ${property.name} = [Error: Unable to access]\n")
            }
        }

        builder.append("$indent}\n")
        return builder.toString()
    }

    // 기본 타입 확인
    private fun isPrimitive(value: Any): Boolean {
        return value is String || value is Number || value is Boolean || value is Char
    }

    // 순환 참조 방지 초기화
    fun reset() {
        visited.clear()
    }
}