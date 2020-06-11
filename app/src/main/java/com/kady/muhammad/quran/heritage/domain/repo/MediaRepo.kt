package com.kady.muhammad.quran.heritage.domain.repo

import com.kady.muhammad.quran.heritage.domain.api.API
import com.kady.muhammad.quran.heritage.domain.ext.sorted
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMediaId
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMediaId
import com.kady.muhammad.quran.heritage.entity.api_response.GetMediaResponse
import com.kady.muhammad.quran.heritage.entity.constant.Const
import com.kady.muhammad.quran.heritage.entity.media.Media
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinComponent
import org.koin.core.inject

object MediaRepo : KoinComponent {

    private val allMediaMutex = Mutex()
    private val allMedia: MutableList<Media> = mutableListOf()
    private val mediaMap: MutableMap<String, List<Media>> = mutableMapOf()
    private val api: API by inject()

    private suspend fun allMedia(force: Boolean = false): List<Media> {
        allMediaMutex.withLock {
            return if (allMedia.isEmpty() || force) {
                if (allMedia.isNotEmpty()) allMedia.clear()
                allMedia.addAll((api.allMedia() as GetMediaResponse).media.sorted())
                allMedia
            } else allMedia
        }
    }

    private suspend fun filterMedia(
        parentMediaId: ParentMediaId,
        force: Boolean = false
    ): List<Media> {
        return (allMedia(force).filter { it.parentId == parentMediaId }).apply {
            mediaMap[parentMediaId] = this
        }
    }

    suspend fun mediaChildrenForParentId(
        parentMediaId: ParentMediaId = Const.MAIN_MEDIA_ID,
        force: Boolean
    ): List<ChildMedia> {
        if (mediaMap[parentMediaId]?.isEmpty() != false || force) return filterMedia(
            parentMediaId,
            force
        )
        return mediaMap[parentMediaId] ?: filterMedia(parentMediaId, force)
    }

    suspend fun parentMediaForChildId(childMediaId: ChildMediaId): ParentMedia {
        val childMedia = allMedia().first { it.id == childMediaId }
        val parentMediaId = childMedia.parentId
        return allMedia().first { it.id == parentMediaId }
    }

    suspend fun otherChildren(childMediaId: ChildMediaId): List<ChildMedia> {
        val childMedia = allMedia().first { it.id == childMediaId }
        val parentMediaId = childMedia.parentId
        return allMedia().filter { it.parentId == parentMediaId && !it.isList }
    }

}