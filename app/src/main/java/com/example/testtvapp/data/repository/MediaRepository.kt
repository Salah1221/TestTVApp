package com.example.testtvapp.data.repository

import com.example.testtvapp.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getMediaItems(): List<MediaItem>
}