package com.example.testtvapp.ui.screens

import android.widget.ProgressBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.example.testtvapp.TestTvApplication
import com.example.testtvapp.data.model.MediaItem
import com.example.testtvapp.ui.components.MediaPlayer
import com.example.testtvapp.ui.viewmodels.HomeUiState
import com.example.testtvapp.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val homeViewModel = viewModel<HomeViewModel>(
        factory = TestTvApplication.container.vmFactory
    )
    val uiState by homeViewModel.mediaItems.collectAsStateWithLifecycle()
    when(val state = uiState) {
        is HomeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading...")
            }
        }
        is HomeUiState.Success -> {
            val mediaItems = state.data
            if (mediaItems.isNotEmpty()) {
                MediaPlayer(mediaItem = mediaItems[0], modifier = modifier.fillMaxSize())
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No media found")
                }
            }
        }
        is HomeUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${state.exception.message}")
            }
        }
    }
}