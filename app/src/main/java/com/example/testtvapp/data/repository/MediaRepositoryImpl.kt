package com.example.testtvapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.model.MediaItemType
import com.example.testtvapp.data.model.Playlist
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepositoryImpl(context: Context, private val httpClient: HttpClient) : MediaRepository {
    private val playlistUrl = "https://api.jsonbin.io/v3/b/69623bb1d0ea881f4061dda1?meta=false"
    private val mediaCacheDir = File(context.filesDir, "media_cache")

    override suspend fun getMediaItems(): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            if (!mediaCacheDir.exists()) {
                mediaCacheDir.mkdirs()
            }
            val remotePlaylist = httpClient
                .get(playlistUrl)
                .body<Playlist>()

            remotePlaylist.items.forEach {
                val fileName = it.url.substringAfterLast('/')
                val localFile = File(mediaCacheDir, fileName)

                if (!localFile.exists()) {
                    val fileBytes: ByteArray = httpClient.get(it.url).body()
                    localFile.writeBytes(fileBytes)
                }
            }

            remotePlaylist.items.map {
                val fileName = it.url.substringAfterLast('/')
                val localFile = File(mediaCacheDir, fileName)

                MediaItem(
                    uri = Uri.fromFile(localFile),
                    name = fileName,
                    type = when(it.type) {
                        "IMAGE" -> MediaItemType.IMAGE
                        "VIDEO" -> MediaItemType.VIDEO
                        else -> MediaItemType.IMAGE
                    }
                )
            }
        }
    }
}