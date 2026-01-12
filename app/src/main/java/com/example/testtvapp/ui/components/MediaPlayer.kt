package com.example.testtvapp.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.SurfaceType
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
private fun VideoPlayer(
    uri: android.net.Uri,
    modifier: Modifier = Modifier,
    onVideoEnd: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            parameters = buildUponParameters()
                .setTunnelingEnabled(false)
                .build()
        }
    }

    // Optimized for local file playback - reduce buffering overhead
    val loadControl = remember {
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2500,   // Min buffer: 2.5s for local files
                10000,  // Max buffer: 10s for local files
                1500,   // Buffer for playback: 1.5s
                2000    // Buffer for playback after rebuffer: 2s
            )
            .build()
    }

    // Hardware decoder preference for 4K playback
    val renderersFactory = remember {
        DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            setEnableDecoderFallback(true)
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setRenderersFactory(renderersFactory)
            .build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
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
        surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
        modifier = modifier.fillMaxSize()
    )
}