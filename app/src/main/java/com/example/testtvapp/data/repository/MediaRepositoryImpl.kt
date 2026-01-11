package com.example.testtvapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.testtvapp.data.model.DownloadProgress
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.model.MediaItemType
import com.example.testtvapp.data.model.MediaLoadState
import com.example.testtvapp.data.model.Playlist
import com.example.testtvapp.data.model.RemoteMediaItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield
import kotlinx.io.readByteArray
import java.io.File
import java.io.FileOutputStream

class MediaRepositoryImpl(context: Context, private val httpClient: HttpClient) : MediaRepository {
    private val playlistUrl = "https://api.jsonbin.io/v3/b/69623bb1d0ea881f4061dda1?meta=false"
    private val mediaCacheDir = File(context.filesDir, "media_cache")

    override suspend fun getMediaItems(): Flow<MediaLoadState> = flow {
        if (!mediaCacheDir.exists()) {
            mediaCacheDir.mkdirs()
        }
        val remotePlaylist = httpClient
            .get(playlistUrl)
            .body<Playlist>()

        val totalItems = remotePlaylist.items.size
        val currentDownloads = mutableMapOf<String, Float>()
        val downloadedMediaItems = mutableListOf<MediaItem>()

        remotePlaylist.items.forEach {
            val fileName = it.url.substringAfterLast('/')
            val localFile = File(mediaCacheDir, fileName)

            if (localFile.exists()) {
                // Correct: Item is already cached/complete
                currentDownloads[fileName] = 1.0f
                downloadedMediaItems += createMediaItem(it, localFile)
            } else {
                // Correct: Item needs to be downloaded, initial progress is 0
                currentDownloads[fileName] = 0f
            }
        }

        val initialProgressList = currentDownloads.map { (name, progress) ->
            DownloadProgress(name, progress, progress == 1.0f)
        }.toList()
        emit(
            MediaLoadState.Progress(
                initialProgressList,
                totalItems,
                downloadedMediaItems.size,
            )
        )

        remotePlaylist.items.forEach {
            val fileName = it.url.substringAfterLast('/')
            val localFile = File(mediaCacheDir, fileName)

            if (localFile.exists()) return@forEach

            val response: HttpResponse = httpClient.get(it.url)
            val totalBytes = response.contentLength() ?: 0L
            var bytesReceived = 0L
            var lastEmittedProgress = 0.0f // Track last progress value sent to the UI

            val inputChannel = response.bodyAsChannel()

            FileOutputStream(localFile).use { outputStream ->
                while (!inputChannel.isClosedForRead) {
                    val packet = inputChannel.readRemaining(8192)

                    if (packet.exhausted()) continue

                    try {
                        val bytes = packet.readByteArray()
                        outputStream.write(bytes)
                        bytesReceived += bytes.size

                        val progress =
                            if (totalBytes > 0)
                                bytesReceived.toFloat() / totalBytes.toFloat()
                            else 0.0f
                        currentDownloads[fileName] = progress

                        // Throttle progress updates: only update if progress increased by 1% or more
                        if (progress - lastEmittedProgress >= 0.01f) {
                            lastEmittedProgress = progress
                            val progressList = currentDownloads.map { (name, p) ->
                                DownloadProgress(name, p, p == 1.0f)
                            }.toList()

                            emit(MediaLoadState.Progress(progressList, totalItems, downloadedMediaItems.size))
                            yield() // Yield control to allow the UI to process the update
                        }
                    } finally {
                        packet.close()
                    }
                }
            }

            currentDownloads[fileName] = 1.0f
            val downloadedItem = createMediaItem(it, localFile)
            downloadedMediaItems.add(downloadedItem)

            val finalProgressList = currentDownloads.map { (name, p) ->
                DownloadProgress(name, p, p == 1.0f)
            }.toList()
            emit(MediaLoadState.Progress(finalProgressList, totalItems, downloadedMediaItems.size))
        }

        emit(MediaLoadState.Success(downloadedMediaItems))
    }.catch { e ->
        emit(MediaLoadState.Error(e as Exception))
    }.flowOn(Dispatchers.IO)

    private fun createMediaItem(item: RemoteMediaItem, localFile: File): MediaItem {
        return MediaItem(
            uri = Uri.fromFile(localFile),
            name = localFile.name,
            type = when(item.type) {
                "IMAGE" -> MediaItemType.IMAGE
                "VIDEO" -> MediaItemType.VIDEO
                else -> MediaItemType.IMAGE
            }
        )
    }
}