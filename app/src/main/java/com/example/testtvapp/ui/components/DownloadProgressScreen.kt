package com.example.testtvapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.testtvapp.data.model.DownloadProgress
import com.example.testtvapp.ui.viewmodels.HomeUiState
import kotlin.math.roundToInt

@Composable
fun DownloadProgressScreen(state: HomeUiState.Loading, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.width(500.dp)
        ) {
            val overallProgress = if (state.totalItems > 0) {
                state.completedCount.toFloat() / state.totalItems.toFloat()
            } else 0f

            Text(
                text = "Preparing Content",
                style = MaterialTheme.typography.headlineMedium
            )

            // --- Custom Linear Progress Indicator ---
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            val progressColor = MaterialTheme.colorScheme.primary
            val barHeight = 8.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .background(trackColor), // Background track color
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        // Use the calculated progress to set the width fraction
                        .fillMaxWidth(overallProgress)
                        .background(progressColor) // Progress fill color
                )
            }

            Text(
                text = "${state.completedCount} of ${state.totalItems} items downloaded",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            val activeDownloads = state.progressUpdates
                .filter { it.progress < 1.0f }
                .sortedBy { it.progress } // Put incomplete items first

            if (activeDownloads.isNotEmpty()) {
                Text(
                    text = "Active Downloads:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Limit the visible list to a few items (e.g., 3 or 4) to prevent clutter
                    activeDownloads.take(4).forEach { download ->
                        DownloadItemRow(download = download)
                    }
                    if (activeDownloads.size > 4) {
                        Text(
                            text = "+${activeDownloads.size - 4} more waiting...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadItemRow(download: DownloadProgress) {
    Card(
        onClick = { /* Do nothing, maybe provide accessibility hint */ },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display filename (or sanitized name)
            Text(
                text = download.fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(16.dp))

            // Display progress percentage
            val percent = (download.progress * 100).roundToInt()
            Text(
                text = if (download.isComplete) "Complete" else "$percent%",
                style = MaterialTheme.typography.bodyMedium,
                color = if (download.isComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        // Secondary, slightly thinner progress bar below the text
        if (!download.isComplete) {
            val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            val progressColor = MaterialTheme.colorScheme.secondary
            val barHeight = 4.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .background(trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(download.progress)
                        .background(progressColor)
                )
            }
        }
    }
}