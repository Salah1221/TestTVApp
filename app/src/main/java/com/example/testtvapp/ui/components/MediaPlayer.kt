package com.example.testtvapp.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import coil.compose.AsyncImage
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.data.model.MediaItemType
import kotlinx.coroutines.delay

@Composable
fun MediaPlayer(mediaItem: MediaItem, modifier: Modifier = Modifier, onMediaEnd: (() -> Unit)? = null) {
    when (mediaItem.type) {
        MediaItemType.IMAGE -> {
            LaunchedEffect(mediaItem.uri) {
                delay(5000L)
                onMediaEnd?.invoke()
            }
            AsyncImage(
                model = mediaItem.uri,
                contentDescription = mediaItem.name,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize(),
            )
        }
        MediaItemType.VIDEO -> {
            VideoPlayer(
                uri = mediaItem.uri,
                modifier = modifier,
                onVideoEnd = onMediaEnd
            )
        }
    }
}

@Composable
private fun VideoPlayer(uri: android.net.Uri, modifier: Modifier = Modifier, onVideoEnd: (() -> Unit)? = null) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onVideoEnd?.invoke()
                    }
                }
            })
        }
    }

    LaunchedEffect(uri) {
        exoPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    PlayerSurface(
        player = exoPlayer,
        modifier = modifier.fillMaxSize()
    )
}