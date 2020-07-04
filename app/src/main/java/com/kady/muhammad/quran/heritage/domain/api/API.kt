package com.kady.muhammad.quran.heritage.domain.api

import android.net.Uri
import com.github.kittinunf.fuel.coroutines.awaitResult
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.entity.api_response.File
import com.kady.muhammad.quran.heritage.entity.api_response.GetMediaResponse
import com.kady.muhammad.quran.heritage.entity.api_response.GetMetadataResponse
import com.kady.muhammad.quran.heritage.entity.api_response.Response
import com.kady.muhammad.quran.heritage.entity.constant.Const.ARCHIVE_DOT_ORG_MP3_FORMAT
import com.kady.muhammad.quran.heritage.entity.constant.Const.MAIN_MEDIA_ID
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.pref.Pref
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class API(private val cc: CoroutineContext, private val pref: Pref, private val mediaRepo: MediaRepo) :
    CoroutineContext by cc {

    companion object {
        private const val BASE_URL = "https://quran25.herokuapp.com"
        private const val ARCHIVE_DOT_ORG_METADATA_BASE_URL = "https://archive.org/metadata"
        private const val ARCHIVE_DOT_ORG_DOWNLOAD_BASE_URL = "https://archive.org/download"
    }

    private suspend fun cacheAllMedia(media: List<Media>) =
        withContext(this) {
            val value = Gson().toJson(media, object : TypeToken<List<Media>>() {}.type)
            pref.saveString("all_media", value)
        }

    suspend fun allMedia(id: String = "195000121950"): Response =
        withContext(this) {
            Uri
                .parse(ARCHIVE_DOT_ORG_METADATA_BASE_URL)
                .buildUpon()
                .appendPath(id)
                .toString()
                .httpGet()
                .awaitResult(GetMetadataResponse.Deserializer(), this@API)
                .component1()
                ?.run { this as GetMetadataResponse }
                ?.run {
                    val media: MutableList<Media> = files
                        .filter { file: File -> file.format == ARCHIVE_DOT_ORG_MP3_FORMAT }
                        .map { file: File ->
                            Media(
                                file.name, metadata.identifier,
                                file.name.substring(0, file.name.lastIndexOf(".")),
                                false
                            )
                        }.toMutableList()
                    media.add(Media(metadata.identifier, MAIN_MEDIA_ID, metadata.title, true))
                    media
                }
                ?.apply { cacheAllMedia(this) }
                ?.run { GetMediaResponse(this) }
                ?: GetMediaResponse(mediaRepo.allCachedMedia())

        }

    fun streamUrl(parentMediaId: String, mediaId: String): String =
        Uri
            .parse(ARCHIVE_DOT_ORG_DOWNLOAD_BASE_URL)
            .buildUpon()
            .appendPath(parentMediaId).appendPath(mediaId)
            .appendQueryParameter("id", mediaId)
            .build()
            .toString()
}