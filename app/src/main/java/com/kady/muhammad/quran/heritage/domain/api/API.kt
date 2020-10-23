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
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.domain.pref.Pref
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class API(
    private val cc: CoroutineContext,
    private val pref: Pref,
    private val mediaRepo: MediaRepo
) :
    CoroutineContext by cc {

    private val allMediaJob = Job()

    private suspend fun cacheAllMedia(media: List<Media>): Boolean =
        withContext(context = this) {
            val value: String = Gson().toJson(media, object : TypeToken<List<Media>>() {}.type)
            return@withContext pref.saveString("all_media", value)
        }

    private suspend fun mediaForIdAsync(id: String, job: Job = Job()): Deferred<Response?> =
        GlobalScope.async(cc + job) {
            Uri
                .parse(ARCHIVE_DOT_ORG_METADATA_BASE_URL)
                .buildUpon()
                .appendPath(id)
                .toString()
                .httpGet()
                .awaitResult(GetMetadataResponse.Deserializer(), this@API)
                .component1()
        }

    private fun File.toMedia(id: String): Media {
        return Media(
            name, id,
            name.substring(0, name.lastIndexOf(".")),
            isList = false
        )
    }

    private suspend fun cacheIfNotEmpty(list: List<Media>, api: API) {
        if (list.isNotEmpty()) api.cacheAllMedia(list)
    }

    suspend fun allMedia(): Response =
        withContext(this) {
            mediaRepo.parentMediaIds()
                .map { mediaForIdAsync(it, allMediaJob) }
                .run { awaitAll(*this.toTypedArray()) }
                .mapNotNull { it as? GetMetadataResponse }
                .filter { it.files.isNotEmpty() }
                .map {
                    it.metadata to
                            it.files.filter { file: File -> file.format == ARCHIVE_DOT_ORG_MP3_FORMAT }
                }
                .flatMap { pair: Pair<Metadata, List<File>> ->
                    val media: MutableList<Media> = pair.second
                        .map { it.toMedia(pair.first.identifier) }.toMutableList()
                    val parentMediaId = pair.first.identifier.split("_").first()
                    media.add(Media(pair.first.identifier, parentMediaId, pair.first.title, true))
                    return@flatMap media
                }
                .apply { cacheIfNotEmpty(this, this@API) }
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


    companion object {
        private const val ARCHIVE_DOT_ORG_METADATA_BASE_URL = "https://archive.org/metadata"
        private const val ARCHIVE_DOT_ORG_DOWNLOAD_BASE_URL = "https://archive.org/download"
    }
}