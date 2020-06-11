package com.kady.muhammad.quran.heritage.entity.api_response

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kady.muhammad.quran.heritage.entity.media.Media

data class GetMediaResponse(@SerializedName("data") val media: List<Media>) : Response {
    class Deserializer : ResponseDeserializable<Response> {
        override fun deserialize(content: String): Response {
            return if (content.isSuccess()) Gson().fromJson(content, GetMediaResponse::class.java)
            else Gson().fromJson(content, FailureResponse::class.java)
        }
    }
}