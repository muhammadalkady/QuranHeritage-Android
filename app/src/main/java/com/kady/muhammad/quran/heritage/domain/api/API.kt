package com.kady.muhammad.quran.heritage.domain.api

import android.net.Uri
import com.github.kittinunf.fuel.coroutines.awaitResult
import com.github.kittinunf.fuel.httpGet
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.entity.api_response.*
import com.kady.muhammad.quran.heritage.entity.media.ParentMediaLocal
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.coroutines.CoroutineContext

class API(
    private val cc: CoroutineContext,
    private val repo: MediaRepo,
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
            val parentMediaIds: List<ParentMediaLocal> = repo.parentMediaIds()
            parentMediaIds
                .map { mediaForIdAsync(it.id, allMediaJob) }
                .run { awaitAll(*this.toTypedArray()) }
                .mapNotNull { it as? GetMetadataResponse }
                .filter { it.files.isNotEmpty() }
                .map { repo.responseToParentMediaData(it) }
                .flatMap { repo.createMediaListFromParentMedia(it, parentMediaIds) }
                .apply { repo.cacheIfNotEmpty(this) }
                .run { GetMediaResponse(this) }
        }

    companion object {
        private const val ARCHIVE_DOT_ORG_METADATA_BASE_URL = "https://archive.org/metadata"
    }
}