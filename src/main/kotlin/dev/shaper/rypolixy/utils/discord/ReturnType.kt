package dev.shaper.rypolixy.utils.discord

sealed class ReturnType<out T,out U> {
    data class Interaction<out T>(val data: T) : ReturnType<T,Nothing>()
    data class Message<out U>    (val data: U) : ReturnType<Nothing,U>()
}