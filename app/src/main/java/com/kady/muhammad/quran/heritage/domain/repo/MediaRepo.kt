package com.kady.muhammad.quran.heritage.domain.repo

import android.app.Application
import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kady.muhammad.quran.heritage.domain.api.API
import com.kady.muhammad.quran.heritage.domain.ext.sorted
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMediaId
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMediaId
import com.kady.muhammad.quran.heritage.entity.api_response.GetMediaResponse
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.media.Media
import com.kady.muhammad.quran.heritage.pref.Pref
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
        return if (fromCache) allCachedMedia()
        else (api.allMedia() as GetMediaResponse).media.sorted()
    }

    private suspend fun filterMedia(fromCache: Boolean, parentMediaId: ParentMediaId): List<Media> {
        return (allMedia(fromCache).filter { it.parentId == parentMediaId })
    }

    suspend fun parentMediaIds(): List<String> = withContext(cc) {
        val json: String = res.openRawResource(res.getIdentifier("media", "raw", packageName))
            .bufferedReader()
            .use { it.readText() }
        return@withContext Gson().fromJson<List<String>>(
            json,
            object : TypeToken<List<String>>() {}.type
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
            Gson().fromJson<List<Media>>(
                pref.getString("all_media", "[]"),
                object : TypeToken<List<Media>>() {}.type
            )
        }


}