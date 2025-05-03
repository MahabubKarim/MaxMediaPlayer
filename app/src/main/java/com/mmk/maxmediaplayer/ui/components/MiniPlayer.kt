package com.mmk.maxmediaplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mmk.maxmediaplayer.domain.model.Track

/**
 * Compact player visible at the bottom of screens during playback
 * @param track Currently playing track
 * @param isPlaying Playback state
 * @param onPlayPause Click handler for play/pause
 * @param modifier Compose modifier
 */
@Composable
fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    track?.let {
        Row(
            modifier = modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track artwork
            AsyncImage(
                model = track.imageUrl,
                contentDescription = "Track artwork",
                modifier = Modifier.size(48.dp)
            )

            Spacer(Modifier.width(8.dp))

            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Play/Pause controls
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
        }
    }
}
