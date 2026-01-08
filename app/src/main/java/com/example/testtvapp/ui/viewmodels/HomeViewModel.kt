package com.example.testtvapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.repository.IMediaRepository
import com.example.testtvapp.data.repository.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val mediaRepository: IMediaRepository): ViewModel() {
    var mediaItems by mutableStateOf<List<MediaItem>>(emptyList())
        private set

    fun getMediaItems() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = mediaRepository.getMediaItems()) {
                is Result.Success -> {
                    mediaItems = result.data
                }
                is Result.Error -> {

                }
            }
        }
    }
}