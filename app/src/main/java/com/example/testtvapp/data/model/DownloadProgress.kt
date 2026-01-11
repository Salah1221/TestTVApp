package com.example.testtvapp.data.model

data class DownloadProgress(
    val fileName: String,
    val progress: Float,
    val isComplete: Boolean,
)

sealed interface MediaLoadState {
    data class Progress(
        val updates: List<DownloadProgress>,
        val totalItems: Int,
        val completedCount: Int,
    ) : MediaLoadState
    data class Success(val items: List<MediaItem>) : MediaLoadState
    data class Error(val exception: Exception) : MediaLoadState
}