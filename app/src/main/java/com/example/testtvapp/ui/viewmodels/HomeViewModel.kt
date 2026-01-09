package com.example.testtvapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtvapp.data.repository.MediaRepository
import com.example.testtvapp.data.repository.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val mediaRepository: MediaRepository): ViewModel() {
    private val _mediaItems = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val mediaItems = _mediaItems.asStateFlow()

    init {
        getMediaItems()
    }

    fun getMediaItems() {
        viewModelScope.launch {
            val result = mediaRepository.getMediaItems()
            _mediaItems.value = result
        }
    }
}