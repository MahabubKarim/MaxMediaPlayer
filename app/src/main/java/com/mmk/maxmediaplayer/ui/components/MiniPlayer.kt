package com.mmk.maxmediaplayer.ui.components

import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.ui.screen.home.HomeViewModel

/**
 * Compact player visible at the bottom of screens during playback
 * @param track Currently playing track
 * @param isPlaying Playback state
 * @param onPlayPause Click handler for play/pause
 * @param modifier Compose modifier
 */
@OptIn(UnstableApi::class)
@Composable
fun MiniPlayer(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController,
    track: Track?,
) {

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    track?.let {
        Box(
            modifier = Modifier
                .padding(12.dp)
                .background(Color.Transparent)
            // We need put a clickable here to run player UI player
        ) {
            Surface(
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable()
                    {
                        navController.navigate("player")
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Track artwork
                    AsyncImage(
                        model = track.imageUrl,
                        contentDescription = "Track artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
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
                    IconButton(onClick = {
                        viewModel.togglePlayback()
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }

                    IconButton(onClick = {
                        /*viewModel.shuffleAll(
                            playerViewModel = playerViewModel,
                            onPlayStart = { track ->
                                ContextCompat.startForegroundService(
                                    context,
                                    Intent(context, PlaybackService::class.java)
                                )
                            },
                            onSuccess = {
                                // navController.navigate("player")
                            }
                        )*/
                    }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle"
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MiniPlayerPreview() {
    val track = Track(
        id = "1",
        title = "Track title",
        artist = "Track artist",
        duration = 120,
        audioUrl = "http://someaudio.com",
        imageUrl = "http://someimage.com"
    )
    MiniPlayer(
        track = track,
        viewModel = hiltViewModel(),
        navController = NavHostController(LocalContext.current)
    )
}
