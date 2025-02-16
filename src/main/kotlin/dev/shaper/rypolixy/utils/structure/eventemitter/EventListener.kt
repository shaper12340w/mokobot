package dev.shaper.rypolixy.utils.structure.eventemitter

interface EventListener<E: Event> {
    fun onEvent(event: E)
}