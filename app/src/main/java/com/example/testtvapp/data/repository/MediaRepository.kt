package com.example.testtvapp.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.model.MediaItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

class MediaRepository(private val context: Context) : IMediaRepository {
    override suspend fun getMediaItems(): Result<List<MediaItem>> {
        return withContext(Dispatchers.IO) {
            val mediaList: MutableList<MediaItem>
            try {
                val projection: Array<String> = arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.MIME_TYPE
                )

                val selection = "${MediaStore.MediaColumns.MIME_TYPE} LIKE ? OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE ?"
                val selectionArgs = arrayOf("image/%", "video/%")

                mediaList = mutableListOf()

                context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    "${MediaStore.MediaColumns.DATE_ADDED} DESC"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                        val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

                        do {
                            val id = cursor.getLong(idColumn)
                            val name = cursor.getString(nameColumn)
                            val mimeType = cursor.getString(mimeColumn)

                            val uri = ContentUris.withAppendedId(
                                MediaStore.Files.getContentUri("external"),
                                id
                            )

                            val type = when {
                                mimeType.startsWith("image/") -> MediaItemType.IMAGE
                                mimeType.startsWith("video/") -> MediaItemType.VIDEO
                                else -> MediaItemType.IMAGE
                            }

                            mediaList += MediaItem(uri, name, type)
                        } while (cursor.moveToNext())
                    }
                }
                Result.Success(mediaList)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.Error(e)
            }
        }
    }
}