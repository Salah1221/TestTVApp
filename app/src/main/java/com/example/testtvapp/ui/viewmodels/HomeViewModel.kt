package com.example.testtvapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.repository.IMediaRepository
import com.example.testtvapp.data.repository.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val mediaRepository: IMediaRepository): ViewModel() {
    var mediaItems by mutableStateOf<Result<List<MediaItem>>>(Result.Loading)

    fun getMediaItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = mediaRepository.getMediaItems()
            mediaItems = result
        }
    }
}