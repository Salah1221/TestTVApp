package com.example.testtvapp.data.repository

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class MediaRepositoryImpl(private val context: Context, private val httpClient: HttpClient) : MediaRepository {
    private val playlistUrl = "https://api.jsonbin.io/v3/b/69623bb1d0ea881f4061dda1?meta=false"
    // Use an external files directory which is compatible with DownloadManager.setDestinationInExternalFilesDir
    private val cacheDirBase = context.getExternalFilesDir(null) ?: throw IllegalStateException("External files directory not available")
    private val mediaCacheDir = File(cacheDirBase, "media_cache")
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Helper class to track download status
    private data class DownloadTracker(
        val name: String,
        var progress: Float,
        var isComplete: Boolean
    )

    override suspend fun getMediaItems(): Flow<MediaLoadState> = flow {
        if (!mediaCacheDir.exists()) {
            mediaCacheDir.mkdirs()
        }

        // 1. Fetch Playlist
        val remotePlaylist = httpClient
            .get(playlistUrl)
            .body<Playlist>()

        val totalItems = remotePlaylist.items.size
        // Key: DownloadId, Value: RemoteMediaItem
        val downloadQueue = mutableMapOf<Long, RemoteMediaItem>()
        // Key: fileName, Value: DownloadState
        val currentDownloads = mutableMapOf<String, DownloadTracker>()
        val downloadedMediaItems = mutableListOf<MediaItem>()

        // 2. Initial setup (check for cached items and enqueue new downloads)
        remotePlaylist.items.forEach { remoteItem ->
            val fileName = remoteItem.url.substringAfterLast('/')
            val localFile = File(mediaCacheDir, fileName)

            if (localFile.exists()) {
                currentDownloads[fileName] = DownloadTracker(fileName, 1.0f, true)
                downloadedMediaItems += createMediaItem(remoteItem, localFile)
            } else {
                val request = DownloadManager.Request(Uri.parse(remoteItem.url))
                    .setTitle("Downloading $fileName")
                    // Save to the media_cache subdirectory within the external files directory.
                    .setDestinationInExternalFilesDir(
                        context,
                        "media_cache",
                        fileName
                    )

                val downloadId = downloadManager.enqueue(request)
                downloadQueue[downloadId] = remoteItem
                currentDownloads[fileName] = DownloadTracker(fileName, 0f, false)
            }
        }

        // 3. Emit initial progress (for already cached items)
        val initialProgressList = currentDownloads.values.map { state ->
            DownloadProgress(state.name, state.progress, state.isComplete)
        }.toList()
        emit(MediaLoadState.Progress(initialProgressList, totalItems, downloadedMediaItems.size))

        if (downloadQueue.isNotEmpty()) {
            // 4. Poll DownloadManager for updates
            val lastEmittedProgressMap = currentDownloads.mapValues { it.value.progress }.toMutableMap()

            while (downloadQueue.isNotEmpty()) {
                val query = DownloadManager.Query().setFilterById(*downloadQueue.keys.toLongArray())
                val cursor: Cursor = downloadManager.query(query)

                while (cursor.moveToNext()) {
                    val downloadId = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val remoteItem = downloadQueue[downloadId] ?: continue
                    val fileName = remoteItem.url.substringAfterLast('/')
                    val downloadState = currentDownloads[fileName] ?: continue

                    val columnIndexBytesTotal = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val columnIndexBytesSoFar = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val localFile = File(mediaCacheDir, fileName)

                            if (downloadState.isComplete.not()) {
                                downloadState.progress = 1.0f
                                downloadState.isComplete = true
                                downloadedMediaItems += createMediaItem(remoteItem, localFile)

                                val progressList = currentDownloads.values.map { state ->
                                    DownloadProgress(state.name, state.progress, state.isComplete)
                                }.toList()
                                emit(MediaLoadState.Progress(progressList, totalItems, downloadedMediaItems.size))
                            }
                            downloadQueue.remove(downloadId)
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                            downloadQueue.remove(downloadId)
                            throw Exception("Download failed for $fileName. Reason: $reason")
                        }
                        DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {
                            val bytesTotal = if (columnIndexBytesTotal >= 0) cursor.getLong(columnIndexBytesTotal) else 0L
                            val bytesSoFar = if (columnIndexBytesSoFar >= 0) cursor.getLong(columnIndexBytesSoFar) else 0L

                            if (bytesTotal > 0) {
                                val progress = bytesSoFar.toFloat() / bytesTotal.toFloat()
                                if (progress > downloadState.progress) {
                                    // Throttle emission: only update if progress increased by 1% or more
                                    if (progress - (lastEmittedProgressMap[fileName] ?: 0.0f) >= 0.01f || progress == 1.0f) {
                                        downloadState.progress = progress
                                        lastEmittedProgressMap[fileName] = progress

                                        val progressList = currentDownloads.values.map { state ->
                                            DownloadProgress(state.name, state.progress, state.isComplete)
                                        }.toList()
                                        emit(MediaLoadState.Progress(progressList, totalItems, downloadedMediaItems.size))
                                    }
                                }
                            }
                        }
                    }
                }
                cursor.close()

                if (downloadQueue.isNotEmpty()) {
                    delay(500) // Wait before polling again
                }
            }
        }

        // 5. Emit final success state
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