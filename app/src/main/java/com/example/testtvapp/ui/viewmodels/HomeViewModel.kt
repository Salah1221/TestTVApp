package com.example.testtvapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtvapp.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data class Success<out T>(val data: T) : HomeUiState
    data class Error(val exception: Exception) : HomeUiState
    data object Loading : HomeUiState
}

class HomeViewModel(private val mediaRepository: MediaRepository): ViewModel() {
    private val _mediaItems = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val mediaItems = _mediaItems.asStateFlow()

    init {
        getMediaItems()
    }

    fun getMediaItems() {
        viewModelScope.launch {
            _mediaItems.value = HomeUiState.Loading
            try {
                val result = mediaRepository.getMediaItems()
                _mediaItems.value = HomeUiState.Success(result)
            } catch (e: Exception) {
                _mediaItems.value = HomeUiState.Error(e)
            }
        }
    }
}