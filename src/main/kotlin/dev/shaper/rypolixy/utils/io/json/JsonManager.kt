package dev.shaper.rypolixy.utils.io.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.shaper.rypolixy.logger
import kotlin.reflect.KClass

class JsonManager(
    var moshi: Moshi? = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()).build()
) {


    inline fun <reified T> decode(json: String): T {
        return moshi?.adapter(T::class.java)?.fromJson(json)
            ?: throw IllegalStateException("Moshi is not initialized")
    }

    inline fun <reified T> encode(obj: T): String {
        return moshi?.adapter(T::class.java)?.toJson(obj)
            ?: throw IllegalStateException("Moshi is not initialized")
    }


    inline fun <reified T : Any> sealedBuilder(
        baseClass: KClass<T>,
        vararg subTypes: KClass<out T>
    ): JsonManager {
        var factory = PolymorphicJsonAdapterFactory.of(baseClass.java, "class_type")
        logger.debug { "Base class: ${baseClass.simpleName}" }
        subTypes.forEach {
            logger.debug { "Sealed class: ${it.simpleName}" }
            factory = factory.withSubtype(it.java, it.simpleName?.lowercase() ?: "Unknown")
        }

        return JsonManager(
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(factory)
                .build()
        )
    }


}