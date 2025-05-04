package com.mmk.maxmediaplayer.ui.screen.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // Collect state
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsState()
    val progress by viewModel.playbackPosition.collectAsState()
    val duration by viewModel.playbackDuration.collectAsState()

    // Auto-update slider animation
    val sliderValue by animateFloatAsState(
        targetValue = progress.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "ProgressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Track Info
        currentTrack?.let { track ->
            AsyncImage(
                model = track.imageUrl,
                contentDescription = "Album art",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(track.title, style = MaterialTheme.typography.headlineMedium)
            Text(track.artist, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = sliderValue,
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(sliderValue.toLong().toTimeString())
                Text(duration.toTimeString())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { viewModel.skipPrevious() }) {
                Icon(Icons.Default.SkipPrevious, "Previous")
            }

            IconButton(onClick = { viewModel.togglePlayback() }) {
                Icon(
                    imageVector = when (playbackState) {
                        is PlaybackState.Playing -> Icons.Default.Pause
                        is PlaybackState.Paused,
                        is PlaybackState.Ready,
                        is PlaybackState.Ended -> Icons.Default.PlayArrow
                        is PlaybackState.Error -> Icons.Default.Error
                        else -> {}
                    } as ImageVector,
                    contentDescription = when (playbackState) {
                        is PlaybackState.Playing -> "Pause"
                        else -> "Play"
                    }
                )
            }

            IconButton(onClick = { viewModel.skipNext() }) {
                Icon(Icons.Default.SkipNext, "Next")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Optional Error Display
        if (playbackState is PlaybackState.Error) {
            Text(
                text = (playbackState as PlaybackState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Extension to format milliseconds
fun Long.toTimeString(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
