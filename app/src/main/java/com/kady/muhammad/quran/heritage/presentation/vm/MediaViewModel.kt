package com.kady.muhammad.quran.heritage.presentation.vm

import android.app.Application
import androidx.lifecycle.*
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.entity.`typealias`.ChildMedia
import com.kady.muhammad.quran.heritage.entity.`typealias`.ParentMediaId
import com.kady.muhammad.quran.heritage.entity.constant.Const
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class MediaViewModel(val app: Application, parentMediaId: ParentMediaId) : AndroidViewModel(app), KoinComponent {

    private val repo: MediaRepo by inject()
    private val _liveMedia: MutableLiveData<List<ChildMedia>> = MutableLiveData()
    val liveMedia: LiveData<List<ChildMedia>> get() = _liveMedia

    init {
        mediaChildrenForParentId(false, parentMediaId)
    }

    fun mediaChildrenForParentId(fromCache: Boolean, parentMediaId: ParentMediaId = Const.MAIN_MEDIA_ID) {
        GlobalScope.launch(Dispatchers.IO) {
            val childMedia = repo.mediaChildrenForParentId(fromCache, parentMediaId)
            _liveMedia.postValue(childMedia)
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