package com.mmk.maxmediaplayer.ui.screen.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    // Observe player state
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.playbackPosition.collectAsState()
    val duration by viewModel.playbackDuration.collectAsState()

    Column(Modifier.fillMaxSize()) {
        // Simple player UI (replace with actual components)
        Text(
            text = currentTrack?.title ?: "No track selected",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = currentTrack?.artist ?: "Unknown artist",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = progress.toFloat(),
            onValueChange = { viewModel.seekTo(it.toLong()) },
            valueRange = 0f..duration.toFloat()
        )
        IconButton(onClick = { viewModel.togglePlayback() }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
        }
    }
}