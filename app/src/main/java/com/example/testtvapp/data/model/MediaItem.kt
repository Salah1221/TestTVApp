package com.example.testtvapp.data.model

import android.net.Uri
import kotlinx.serialization.Serializable

enum class MediaItemType {
    IMAGE,
    VIDEO
}

data class MediaItem(val uri: Uri, val name: String, val type: MediaItemType)

@Serializable
data class RemoteMediaItem(
    val id: String,
    val type: String,
    val url: String
)

@Serializable
data class Playlist(val items: List<RemoteMediaItem>)