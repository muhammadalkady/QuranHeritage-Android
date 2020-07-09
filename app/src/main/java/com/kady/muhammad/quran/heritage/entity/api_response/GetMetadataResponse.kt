package com.kady.muhammad.quran.heritage.entity.api_response

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class GetMetadataResponse(
    @SerializedName("files") val files: List<File> = emptyList(),
    @SerializedName("metadata") val metadata: Metadata = Metadata("", "")
) : Response {
    class Deserializer : ResponseDeserializable<Response> {
        override fun deserialize(content: String): Response {
            return Gson().fromJson(content, GetMetadataResponse::class.java)
        }
    }
}

data class File(
    @SerializedName("format") val format: String,
    @SerializedName("name") val name: String,
    @SerializedName("original") val original: String,
    @SerializedName("source") val source: String
)

data class Metadata(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("title") val title: String
)