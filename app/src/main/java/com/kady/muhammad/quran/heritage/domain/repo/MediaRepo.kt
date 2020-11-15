package com.kady.muhammad.quran.heritage.domain.repo

import android.app.Application
import android.content.res.Resources
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kady.muhammad.quran.heritage.domain.api.API
import com.kady.muhammad.quran.heritage.domain.db.DB
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMediaId
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMediaId
import com.kady.muhammad.quran.heritage.entity.api_response.File
import com.kady.muhammad.quran.heritage.entity.api_response.GetMetadataResponse
import com.kady.muhammad.quran.heritage.entity.api_response.Metadata
import com.kady.muhammad.quran.heritage.entity.api_response.Response
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.ext.toMedia
import com.kady.muhammad.quran.heritage.entity.media.FavoriteMedia
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.entity.media.ParentMediaData
import com.kady.muhammad.quran.heritage.entity.media.ParentMediaLocal
import com.kady.muhammad.quran.heritage.entity.reciter.Reciter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

class MediaRepo(private val cc: CoroutineContext) : KoinComponent {

    private val app: Application by inject()
    private val api: API by inject()
    private val res: Resources = app.resources
    private val packageName: String = app.packageName
    private val db: DB = DB

    private fun filterMedia(parentMediaId: ParentMediaId): Flow<List<Media>> {
        return allMediaLocal().combine(db.getAllFavorite()) { allMedia, _ -> allMedia }
            .map { allMedia -> allMedia.filter { it.parentId == parentMediaId } }
    }

    private suspend fun recitersToMedia(): List<Media> {
        return reciters().map {
            Media(it.id, Const.MAIN_MEDIA_ID, it.name, EMPTY_PARENT_MEDIA_TITLE, true)
        }
    }

    private suspend fun reciters(): List<Reciter> = withContext(cc) {
        val json: String =
            res.openRawResource(res.getIdentifier(RAW_RECITERS_FILE_NAME, RAW, packageName))
                .bufferedReader()
                .use { it.readText() }
        return@withContext Gson().fromJson<List<Reciter>>(
            json,
            object : TypeToken<List<Reciter>>() {}.type
        )
    }

    private suspend fun cacheAllMedia(media: List<Media>): Unit =
        withContext(context = cc) {
            val oldMediaIds: List<String> = db.getAllMedia().first().map { it.id }
            val newMediaIds: List<String> = media.map { it.id }
            val deletedMediaIds: List<String> = oldMediaIds.minus(newMediaIds)
            Logger.logI(tag = "DB", msg = "deletedMediaIds = $deletedMediaIds")
            val deletedMediaCount: Int = db.deleteMedia(deletedMediaIds)
            Logger.logI(tag = "DB", msg = "deletedMediaCount = $deletedMediaCount")
            val deletedFavoriteMediaCount: Int = db.deleteFavorite(deletedMediaIds)
            Logger.logI(tag = "DB", msg = "deletedFavoriteMediaCount = $deletedFavoriteMediaCount")
            val insertedMediaCount: Int = db.insertAllMedia(media).size
            Logger.logI(tag = "DB", msg = "insertedMediaCount = $insertedMediaCount")
            Unit
        }

    private fun parentMediaToMedia(it: ParentMediaData): List<Media> {
        return it.files.map { file -> file.toMedia(it.metadata.identifier, it.metadata.title) }
    }

    private fun createParentMedia(metadata: Metadata, parentMediaId: String): Media {
        return Media(
            id = metadata.identifier,
            parentId = parentMediaId,
            title = metadata.title,
            parentTitle = EMPTY_PARENT_MEDIA_TITLE,
            isList = true
        )
    }

    private fun parentMediaId(parentMediaIds: List<ParentMediaLocal>, metadata: Metadata) =
        parentMediaIds.first { it.id == metadata.identifier }.parentId

    private suspend fun addToFavorite(id: String) {
        withContext(cc) { db.insertFavorite(FavoriteMedia(id)) }
    }

    private suspend fun deleteFromFavorite(id: String) {
        withContext(cc) { db.deleteFavorite(listOf(id)) }
    }

    suspend fun toggleFavorite(id: String) = withContext(cc) {
        if (db.isFavorite(id)) deleteFromFavorite(id) else addToFavorite(id)
    }

    fun allMediaLocal(): Flow<List<Media>> {
        return db.getAllMedia().map { it + recitersToMedia() }
    }

    suspend fun getAllMedia(): Response {
        return api.allMedia()
    }

    suspend fun cacheIfNotEmpty(list: List<Media>) {
        if (list.isNotEmpty()) cacheAllMedia(list)
    }

    fun responseToParentMediaData(it: GetMetadataResponse): ParentMediaData {
        val files =
            it.files.filter { file: File -> file.format == Const.ARCHIVE_DOT_ORG_MP3_FORMAT }
        return ParentMediaData(it.metadata, files)
    }

    fun createMediaListFromParentMedia(
        it: ParentMediaData,
        parentMediaIds: List<ParentMediaLocal>
    ): MutableList<Media> {
        val media: MutableList<Media> = parentMediaToMedia(it).toMutableList()
        val parentMediaId: String = parentMediaId(parentMediaIds, it.metadata)
        media.add(createParentMedia(it.metadata, parentMediaId))
        return media
    }

    suspend fun parentMediaIds(): List<ParentMediaLocal> = withContext(cc) {
        val json: String =
            res.openRawResource(res.getIdentifier(RAW_MEDIA_FILE_NAME, RAW, packageName))
                .bufferedReader()
                .use { it.readText() }
        return@withContext Gson().fromJson<List<ParentMediaLocal>>(
            json,
            object : TypeToken<List<ParentMediaLocal>>() {}.type
        )
    }

    fun count(): Flow<Int> {
        return allMediaLocal().map { allMedia -> allMedia.filterNot { it.isList }.size }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    fun mediaChildrenForParentId(parentMediaId: ParentMediaId = Const.MAIN_MEDIA_ID): Flow<List<ChildMedia>> {
        if (parentMediaId == Const.FAVORITE_MEDIA_ID) {
            return db.getAllFavorite()
                .flatMapConcat { db.getMedia(it.map { favoriteMedia -> favoriteMedia.id }) }
        }
        return filterMedia(parentMediaId)
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    fun parentMediaForChildId(childMediaId: ChildMediaId): Flow<ParentMedia> {
        val childMedia: Flow<Media> =
            allMediaLocal().map { allMedia -> allMedia.first { it.id == childMediaId } }
        val parentMediaId: Flow<String> = childMedia.map { it.parentId }
        return parentMediaId.flatMapConcat { allMediaLocal().map { allMedia -> allMedia.first { media -> media.id == it } } }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    fun otherChildren(childMediaId: ChildMediaId): Flow<List<ChildMedia>> {
        val childMedia =
            allMediaLocal().map { allMedia -> allMedia.first { it.id == childMediaId } }
        val parentMediaId = childMedia.map { it.parentId }
        return parentMediaId.flatMapConcat {
            allMediaLocal()
                .map { allMedia -> allMedia.filter { media -> media.parentId == it && !media.isList } }
        }
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
        private const val ARCHIVE_DOT_ORG_DOWNLOAD_BASE_URL = "https://archive.org/download"
        const val RAW_MEDIA_FILE_NAME = "media"
        const val RAW_RECITERS_FILE_NAME = "reciters"
        const val EMPTY_PARENT_MEDIA_TITLE = ""
        const val RAW = "raw"
    }

}