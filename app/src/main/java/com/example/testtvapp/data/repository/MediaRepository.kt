package com.example.testtvapp.data.repository

import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.model.MediaLoadState
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getMediaItems(): Flow<MediaLoadState>
}