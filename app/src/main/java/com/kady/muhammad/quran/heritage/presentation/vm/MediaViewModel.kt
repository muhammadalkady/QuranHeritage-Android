package com.kady.muhammad.quran.heritage.presentation.vm

import android.app.Application
import androidx.lifecycle.*
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMediaId
import com.kady.muhammad.quran.heritage.entity.constant.Const
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class MediaViewModel(val app: Application, parentMediaId: ParentMediaId) : AndroidViewModel(app), KoinComponent {

    private val repo: MediaRepo by inject()
    private val _liveMedia: MutableLiveData<List<ChildMedia>> = MutableLiveData()
    private val _liveMediaCount: MutableLiveData<Int> = MutableLiveData()
    private val _liveLoading: MutableLiveData<Boolean> = MutableLiveData()
    val liveMedia: LiveData<List<ChildMedia>> get() = _liveMedia
    val liveMediaCount: LiveData<Int> get() = _liveMediaCount
    val liveLoading: LiveData<Boolean> get() = _liveLoading

    init {
        mediaChildrenForParentId(false, parentMediaId)
    }

    fun mediaChildrenForParentId(fromCache: Boolean, parentMediaId: ParentMediaId = Const.MAIN_MEDIA_ID) {
        viewModelScope.launch(Dispatchers.IO) {
            _liveLoading.postValue(true)
            _liveMedia.postValue(repo.mediaChildrenForParentId(fromCache = true, parentMediaId = parentMediaId))
            _liveMediaCount.postValue(repo.count())
            _liveMedia.postValue(repo.mediaChildrenForParentId(fromCache, parentMediaId))
            _liveMediaCount.postValue(repo.count())
            _liveLoading.postValue(false)
        }
    }

}

class MediaViewModelFactory(val app: Application, private val parentMediaId: ParentMediaId) :
    ViewModelProvider.AndroidViewModelFactory(app) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MediaViewModel(app, parentMediaId) as T
    }
}