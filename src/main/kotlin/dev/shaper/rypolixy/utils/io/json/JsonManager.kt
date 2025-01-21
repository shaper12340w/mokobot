package dev.shaper.rypolixy.utils.io.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonManager {

    val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    inline fun <reified T> encode(string: String): T{
        return (moshi.adapter(T::class.java) as JsonAdapter<T>).fromJson(string)!!
    }

    inline fun <reified T> decode(classVal:T): String{
        return (moshi.adapter(T::class.java) as JsonAdapter<T>).toJson(classVal)!!
    }

}