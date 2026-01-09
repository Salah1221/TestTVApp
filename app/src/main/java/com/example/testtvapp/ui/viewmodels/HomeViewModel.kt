package com.example.testtvapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data class Success(val data: List<MediaItem>) : HomeUiState
    data class Error(val exception: Exception) : HomeUiState
    data object Loading : HomeUiState
}

class HomeViewModel(private val mediaRepository: MediaRepository): ViewModel() {
    private val _mediaItems = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val mediaItems = _mediaItems.asStateFlow()
    private val _mediaIndex = MutableStateFlow<Int?>(null)
    val mediaIndex = _mediaIndex.asStateFlow()

    fun getMediaItems() {
        viewModelScope.launch {
            _mediaItems.value = HomeUiState.Loading
            try {
                val result = mediaRepository.getMediaItems()
                _mediaItems.value = HomeUiState.Success(result)
                _mediaIndex.value = 0
            } catch (e: Exception) {
                _mediaItems.value = HomeUiState.Error(e)
            }
        }
    }

    fun nextMedia(size: Int) {
        _mediaIndex.value = _mediaIndex.value?.plus(1)?.rem(size)
    }
}