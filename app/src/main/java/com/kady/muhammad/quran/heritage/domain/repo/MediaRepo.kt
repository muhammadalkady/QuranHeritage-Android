package com.kady.muhammad.quran.heritage.domain.repo

import android.app.Application
import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kady.muhammad.quran.heritage.domain.api.API
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMediaId
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMediaId
import com.kady.muhammad.quran.heritage.entity.api_response.GetMediaResponse
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.domain.pref.Pref
import com.kady.muhammad.quran.heritage.entity.api_response.Metadata
import com.kady.muhammad.quran.heritage.entity.media.ParentLocalMedia
import com.kady.muhammad.quran.heritage.entity.reciter.Reciter
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

class MediaRepo(private val cc: CoroutineContext, private val pref: Pref) : KoinComponent {

    private val api: API by inject()
    private val app: Application by inject()
    private val res: Resources = app.resources
    private val packageName: String = app.packageName

    private suspend fun allMedia(fromCache: Boolean): List<Media> {
        val allMedia: List<Media> = if (fromCache) allCachedMedia()
        else (api.allMedia() as GetMediaResponse).media
        return allMedia + recitersToMedia()
    }

    private suspend fun filterMedia(fromCache: Boolean, parentMediaId: ParentMediaId): List<Media> {
        return (allMedia(fromCache).filter { it.parentId == parentMediaId })
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

    private suspend fun cacheAllMedia(media: List<Media>): Boolean =
        withContext(context = cc) {
            val value: String = Gson().toJson(media, object : TypeToken<List<Media>>() {}.type)
            return@withContext pref.saveString(ALL_MEDIA_KEY, value)
        }

    fun createParentMedia(metadata: Metadata, parentMediaId: String): Media {
        return Media(
            id = metadata.identifier,
            parentId = parentMediaId,
            title = metadata.title,
            parentTitle = EMPTY_PARENT_MEDIA_TITLE,
            isList = true
        )
    }

    fun parentMediaId(parentMediaIds: List<ParentLocalMedia>, metadata: Metadata) =
        parentMediaIds.first { it.id == metadata.identifier }.parentId

    suspend fun cacheIfNotEmpty(list: List<Media>) {
        if (list.isNotEmpty()) cacheAllMedia(list)
    }

    suspend fun parentMediaIds(): List<ParentLocalMedia> = withContext(cc) {
        val json: String =
            res.openRawResource(res.getIdentifier(RAW_MEDIA_FILE_NAME, RAW, packageName))
                .bufferedReader()
                .use { it.readText() }
        return@withContext Gson().fromJson<List<ParentLocalMedia>>(
            json,
            object : TypeToken<List<ParentLocalMedia>>() {}.type
        )
    }

    suspend fun count(): Int {
        return allCachedMedia().filterNot { it.isList }.size
    }

    suspend fun mediaChildrenForParentId(
        fromCache: Boolean,
        parentMediaId: ParentMediaId = Const.MAIN_MEDIA_ID
    ): List<ChildMedia> {
        return filterMedia(fromCache, parentMediaId)
    }

    suspend fun parentMediaForChildId(fromCache: Boolean, childMediaId: ChildMediaId): ParentMedia {
        val childMedia: Media = allMedia(fromCache).first { it.id == childMediaId }
        val parentMediaId: String = childMedia.parentId
        return allMedia(fromCache).first { it.id == parentMediaId }
    }

    suspend fun otherChildren(fromCache: Boolean, childMediaId: ChildMediaId): List<ChildMedia> {
        val childMedia: Media = allMedia(fromCache).first { it.id == childMediaId }
        val parentMediaId: String = childMedia.parentId
        return allMedia(fromCache).filter { it.parentId == parentMediaId && !it.isList }
    }

    suspend fun allCachedMedia(): List<Media> =
        withContext(cc) {
            Gson().fromJson(
                pref.getString(ALL_MEDIA_KEY, ALL_MEDIA_DEFAULT_VALUE),
                object : TypeToken<List<Media>>() {}.type
            )
        }

    companion object {
        const val ALL_MEDIA_KEY = "all_media"
        const val ALL_MEDIA_DEFAULT_VALUE = "[]"
        const val RAW_MEDIA_FILE_NAME = "media"
        const val RAW_RECITERS_FILE_NAME = "reciters"
        const val EMPTY_PARENT_MEDIA_TITLE = ""
        const val RAW = "raw"
    }

}