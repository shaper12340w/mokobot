package dev.shaper.rypolixy.utils.structure.eventemitter

import kotlin.reflect.KClass

open class EventEmitter {
    val listeners = mutableMapOf<KClass<out Event>, MutableList<EventListener<out Event>>>()

    fun <E : Event> emit(event: E) {
        listeners[event::class]
            ?.filterIsInstance<EventListener<E>>()
            ?.toList()
            ?.forEach { it.onEvent(event) }
    }

    inline fun <reified E : Event> on(listener: EventListener<E>) {
        val eventListeners = listeners.getOrPut(E::class) {
            mutableListOf()
        }
        eventListeners.add(listener)
    }

    inline fun <reified E : Event> off(listener: EventListener<E>) {
        listeners[E::class]?.remove(listener)
    }

    inline fun <reified E : Event> once(listener: EventListener<E>) {
        val wrapper = object : EventListener<E> {
            override fun onEvent(event: E) {
                off(this)
                listener.onEvent(event)
            }
        }
        on(wrapper)
    }

}