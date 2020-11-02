package com.kady.muhammad.quran.heritage.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.entity.media.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class SearchViewModel : ViewModel(), KoinComponent {

    private val repo: MediaRepo by inject()
    private val _searchResult = MutableLiveData<List<Media>>()
    private val cashedMedia = viewModelScope.async(Dispatchers.IO) { repo.allCachedMedia() }
    private var searchJob: Job? = null

    //
    val searchQuery = MutableLiveData<String>()
    val searchResult: LiveData<List<Media>> = _searchResult

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResult.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch(Dispatchers.Default) {
            val cachedMedia = cashedMedia.await()
            _searchResult.postValue(cachedMedia.filter { it.title.contains(query.trim(), true) }
                .sortedBy { !it.isList })
        }
    }

    override fun onCleared() {
        searchJob?.cancel()
    }

}