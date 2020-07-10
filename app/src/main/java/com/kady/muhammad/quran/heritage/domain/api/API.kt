package com.kady.muhammad.quran.heritage.domain.api

import android.net.Uri
import com.github.kittinunf.fuel.coroutines.awaitResult
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.entity.api_response.*
import com.kady.muhammad.quran.heritage.entity.constant.Const.ARCHIVE_DOT_ORG_MP3_FORMAT
import com.kady.muhammad.quran.heritage.entity.constant.Const.MAIN_MEDIA_ID
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.pref.Pref
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class API(private val cc: CoroutineContext, private val pref: Pref, private val mediaRepo: MediaRepo) :
    CoroutineContext by cc {

    companion object {
        private const val ARCHIVE_DOT_ORG_METADATA_BASE_URL = "https://archive.org/metadata"
        private const val ARCHIVE_DOT_ORG_DOWNLOAD_BASE_URL = "https://archive.org/download"
    }

    private suspend fun cacheAllMedia(media: List<Media>) =
        withContext(this) {
            val value = Gson().toJson(media, object : TypeToken<List<Media>>() {}.type)
            pref.saveString("all_media", value)
        }

    suspend fun allMedia(ids: List<String> = mediaRepo.parentMediaIds()): Response =
        withContext(this) {
            ids
                .map { id ->
                    Uri
                        .parse(ARCHIVE_DOT_ORG_METADATA_BASE_URL)
                        .buildUpon()
                        .appendPath(id)
                        .toString()
                        .httpGet()
                        .awaitResult(GetMetadataResponse.Deserializer(), this@API)
                        .component1()
                }.mapNotNull { it as? GetMetadataResponse }
                .filter { it.files.isNotEmpty() }
                .map { Pair(it.metadata, it.files.filter { file: File -> file.format == ARCHIVE_DOT_ORG_MP3_FORMAT }) }
                .flatMap { pair: Pair<Metadata, List<File>> ->
                    val media = pair.second.map { file ->
                        Media(
                            file.name, pair.first.identifier,
                            file.name.substring(0, file.name.lastIndexOf(".")),
                            false
                        )
                    }.toMutableList()
                    media.add(Media(pair.first.identifier, MAIN_MEDIA_ID, pair.first.title, true))
                    media
                }
                .apply { if (isNotEmpty()) cacheAllMedia(this) }
                .run { if (isEmpty()) mediaRepo.allCachedMedia() else this }
                .run { GetMediaResponse(this) }
                .apply { Logger.logI("Calling API", "response $this") }
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