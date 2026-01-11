package com.example.testtvapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtvapp.data.model.DownloadProgress
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.model.MediaLoadState
import com.example.testtvapp.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data class Success(val data: List<MediaItem>) : HomeUiState
    data class Error(val exception: Exception) : HomeUiState
    data class Loading(
        val progressUpdates: List<DownloadProgress> = emptyList(),
        val totalItems: Int = 0,
        val completedCount: Int = 0
    ) : HomeUiState
}

class HomeViewModel(private val mediaRepository: MediaRepository): ViewModel() {
    private val _mediaItems = MutableStateFlow<HomeUiState>(HomeUiState.Loading())
    val mediaItems = _mediaItems.asStateFlow()
    private val _mediaIndex = MutableStateFlow<Int?>(null)
    val mediaIndex = _mediaIndex.asStateFlow()

    init {
        getMediaItems()
    }

    fun getMediaItems() {
        viewModelScope.launch {
            _mediaItems.value = HomeUiState.Loading()
            try {
                mediaRepository.getMediaItems().collect { loadState ->
                    when (loadState) {
                        is MediaLoadState.Progress -> {
                            _mediaItems.value = HomeUiState.Loading(
                                progressUpdates = loadState.updates,
                                totalItems = loadState.totalItems,
                                completedCount = loadState.completedCount
                            )
                        }
                        is MediaLoadState.Success -> {
                            _mediaItems.value = HomeUiState.Success(loadState.items)
                            _mediaIndex.value = 0
                        }
                        is MediaLoadState.Error -> {
                            _mediaItems.value = HomeUiState.Error(loadState.exception)
                        }
                    }
                }
            } catch (e: Exception) {
                _mediaItems.value = HomeUiState.Error(e)
            }
        }
    }

    fun nextMedia(size: Int) {
        _mediaIndex.value = _mediaIndex.value?.plus(1)?.rem(size)
    }
}