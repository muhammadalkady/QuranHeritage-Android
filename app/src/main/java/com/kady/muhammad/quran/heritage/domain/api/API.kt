package com.kady.muhammad.quran.heritage.domain.api

import android.net.Uri
import com.github.kittinunf.fuel.coroutines.awaitResult
import com.github.kittinunf.fuel.httpGet
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo.Companion.EMPTY_PARENT_MEDIA_TITLE
import com.kady.muhammad.quran.heritage.entity.api_response.*
import com.kady.muhammad.quran.heritage.entity.constant.Const.ARCHIVE_DOT_ORG_MP3_FORMAT
import com.kady.muhammad.quran.heritage.entity.ext.toMedia
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.entity.media.ParentLocalMedia
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class API(
    private val cc: CoroutineContext,
    private val mediaRepo: MediaRepo,
) :
    CoroutineContext by cc {

    private val allMediaJob = Job()

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

    suspend fun allMedia(): Response =
        withContext(this) {
            val parentMediaIds: List<ParentLocalMedia> = mediaRepo.parentMediaIds()
            parentMediaIds
                .map { mediaForIdAsync(it.id, allMediaJob) }
                .run { awaitAll(*this.toTypedArray()) }
                .mapNotNull { it as? GetMetadataResponse }
                .filter { it.files.isNotEmpty() }
                .map {
                    it.metadata to
                            it.files.filter { file: File -> file.format == ARCHIVE_DOT_ORG_MP3_FORMAT }
                }
                .flatMap { pair: Pair<Metadata, List<File>> ->
                    val media: MutableList<Media> =
                        pair.second.map { it.toMedia(pair.first.identifier, pair.first.title) }
                            .toMutableList()
                    val parentMediaId: String = mediaRepo.parentMediaId(parentMediaIds, pair.first)
                    media.add(mediaRepo.createParentMedia(pair.first, parentMediaId))
                    return@flatMap media
                }
                .apply { mediaRepo.cacheIfNotEmpty(this) }
                .run { if (isEmpty()) mediaRepo.allCachedMedia() else this }
                .run { GetMediaResponse(this) }
        }

    fun streamUrl(parentMediaId: String, mediaId: String): String =
        Uri
            .parse(ARCHIVE_DOT_ORG_DOWNLOAD_BASE_URL)
            .buildUpon()
            .appendPath(parentMediaId).appendPath(mediaId.split("_").last())
            .appendQueryParameter("id", mediaId)
            .build()
            .toString()


    companion object {
        private const val ARCHIVE_DOT_ORG_METADATA_BASE_URL = "https://archive.org/metadata"
        private const val ARCHIVE_DOT_ORG_DOWNLOAD_BASE_URL = "https://archive.org/download"
    }
}