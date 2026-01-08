package com.example.testtvapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.repository.MediaRepository
import com.example.testtvapp.data.repository.Result
import kotlinx.coroutines.launch

class HomeViewModel(private val mediaRepository: MediaRepository): ViewModel() {
    var mediaItems by mutableStateOf<Result<List<MediaItem>>>(Result.Loading)

    fun getMediaItems() {
        viewModelScope.launch {
            val result = mediaRepository.getMediaItems()
            mediaItems = result
        }
    }
}