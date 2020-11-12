package com.kady.muhammad.quran.heritage.presentation.vm

import android.app.Application
import androidx.lifecycle.*
import com.kady.muhammad.quran.heritage.domain.log.Logger
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMediaId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class MediaViewModel(val app: Application, parentMediaId: ParentMediaId) : AndroidViewModel(app),
    KoinComponent {

    private val repo: MediaRepo by inject()

    //
    private val _liveLoading: MutableLiveData<Boolean> = MutableLiveData()
    val liveLoading: LiveData<Boolean> get() = _liveLoading

    //
    val liveMedia: LiveData<List<ChildMedia>> = liveData(Dispatchers.IO) {
        _liveLoading.postValue(true)
        repo.mediaChildrenForParentId(parentMediaId = parentMediaId).collect {
            Logger.logI("Media", "collect size = ${it.size}")
            emit(it)
            _liveLoading.postValue(false)
        }
    }

    //
    val liveMediaCount: LiveData<Int> = liveData(Dispatchers.IO) {
        repo.count().collect {
            Logger.logI("Media", "collect media count = $it")
            emit(it)
        }
    }
    //

    init {
        getAllMedia()
    }

    fun getAllMedia() {
        _liveLoading.value = true
        viewModelScope.launch(Dispatchers.IO) { repo.getAllMedia() }
    }

    fun toggleFavorite(id: String) = liveData(Dispatchers.IO) {
        emit(repo.toggleFavorite(id))
    }

}

class MediaViewModelFactory(val app: Application, private val parentMediaId: ParentMediaId) :
    ViewModelProvider.AndroidViewModelFactory(app) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MediaViewModel(app, parentMediaId) as T
    }
}