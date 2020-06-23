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
    private val api: API by inject()

    private suspend fun allMedia(fromCache: Boolean): List<Media> {
        return allMediaMutex.withLock {
            if (fromCache && allMedia.isNotEmpty()) return@withLock allMedia
            (api.allMedia() as GetMediaResponse).media
                .sorted().apply {
                    allMedia.clear()
                    allMedia.addAll(this)
                }
        }
    }

    private suspend fun filterMedia(fromCache: Boolean, parentMediaId: ParentMediaId): List<Media> {
        return (allMedia(fromCache).filter { it.parentId == parentMediaId })
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

}