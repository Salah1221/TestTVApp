package com.example.testtvapp.data.repository

import com.example.testtvapp.data.model.MediaItem

interface IMediaRepository {
    suspend fun getMediaItems(): Result<List<MediaItem>>
}