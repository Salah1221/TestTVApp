package com.example.testtvapp.data.model

import android.net.Uri

enum class MediaItemType {
    IMAGE,
    VIDEO
}

data class MediaItem(val uri: Uri, val name: String, val type: MediaItemType)