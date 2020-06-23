package com.kady.muhammad.quran.heritage.domain.api

import android.net.Uri
import com.github.kittinunf.fuel.coroutines.awaitResult
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.api_response.GetMediaResponse
import com.kady.muhammad.quran.heritage.entity.api_response.Response
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.pref.Pref
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class API(private val cc: CoroutineContext, private val pref: Pref) :
    CoroutineContext by cc {

    companion object {
        private const val BASE_URL = "https://quran25.herokuapp.com"
    }

    private suspend fun cacheAllMedia(media: List<Media>) =
        withContext(this) {
            val value = Gson().toJson(media, object : TypeToken<List<Media>>() {}.type)
            pref.saveString("all_media", value)
        }

    private suspend fun allCachedMedia() =
        withContext(this) {
            Gson().fromJson<List<Media>>(
                pref.getString("all_media", "[]"),
                object : TypeToken<List<Media>>() {}.type
            )
        }

    suspend fun allMedia(): Response {
        return Uri.parse(BASE_URL)
            .buildUpon()
            .appendPath("get").appendPath("media")
            .toString().httpGet()
            .awaitResult(GetMediaResponse.Deserializer(), this)
            .component1()
            ?.apply { if (this is GetMediaResponse) cacheAllMedia(media) }
            ?.apply { Logger.logI("Calling API", "all media = ${(this as GetMediaResponse).media.size}") }
            ?: GetMediaResponse(allCachedMedia())
    }

    fun streamUrl(mediaId: String): String =
        Uri
            .parse(BASE_URL)
            .buildUpon()
            .appendPath("stream").appendPath("media")
            .appendQueryParameter("id", mediaId)
            .build()
            .toString()
}