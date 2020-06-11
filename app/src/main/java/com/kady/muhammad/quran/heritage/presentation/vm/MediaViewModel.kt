package com.kady.muhammad.quran.heritage.presentation.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMediaId
import com.kady.muhammad.quran.heritage.entity.constant.Const

class MediaViewModel(val app: Application) : AndroidViewModel(app) {

    fun mediaChildrenForParentId(
        parentMediaId: ParentMediaId = Const.MAIN_MEDIA_ID,
        force: Boolean
    ): LiveData<List<ChildMedia>> =
        liveData {
            emit(MediaRepo.mediaChildrenForParentId(parentMediaId, force))
        }

}