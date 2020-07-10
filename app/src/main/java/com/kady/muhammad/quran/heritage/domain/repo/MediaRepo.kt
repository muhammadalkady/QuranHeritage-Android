package com.kady.muhammad.quran.heritage.domain.repo

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

    private suspend fun allMedia(fromCache: Boolean): List<Media> {
        return if (fromCache) allCachedMedia()
        else (api.allMedia() as GetMediaResponse).media.sorted()
    }

    private suspend fun filterMedia(fromCache: Boolean, parentMediaId: ParentMediaId): List<Media> {
        return (allMedia(fromCache).filter { it.parentId == parentMediaId })
    }

    fun parentMediaIds(): List<String> {
        return listOf(
            "basit_1950",
            "basit_1951_",
            "basit_1952",
            "basit_1953",
            "basit_1954",
            "basit_1955",
            "basit_1956",
            "basit_1957",
            "basit_1958",
            "basit_1959",
            "basit_1960"
        )
    }

    suspend fun count(): Int {
        return allCachedMedia().filterNot { it.isList }.size
    }

    suspend fun mediaChildrenForParentId(fromCache: Boolean, parentMediaId: ParentMediaId = Const.MAIN_MEDIA_ID): List<ChildMedia> {
        return filterMedia(fromCache, parentMediaId)
    }

    suspend fun parentMediaForChildId(fromCache: Boolean, childMediaId: ChildMediaId): ParentMedia {
        val childMedia = allMedia(fromCache).first { it.id == childMediaId }
        val parentMediaId = childMedia.parentId
        return allMedia(fromCache).first { it.id == parentMediaId }
    }

    suspend fun otherChildren(fromCache: Boolean, childMediaId: ChildMediaId): List<ChildMedia> {
        val childMedia = allMedia(fromCache).first { it.id == childMediaId }
        val parentMediaId = childMedia.parentId
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