package com.mmk.maxmediaplayer.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mmk.maxmediaplayer.R
import com.mmk.maxmediaplayer.ui.model.TrackItem

@Composable
fun TrackListItem(
    track: TrackItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art placeholder
        Icon(
            painter = painterResource(id = R.drawable.ic_music_note),
            contentDescription = "Album art",
            modifier = Modifier.size(48.dp)
        )

        // Track info
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        )

        // Duration
        Text(
            text = track.duration,
            style = MaterialTheme.typography.bodyMedium
        )

        // Play/pause button
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(
                    id = if (track.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                ),
                contentDescription = if (track.isPlaying) "Pause" else "Play"
            )
        }
    }
}

@Preview
@Composable
fun TrackListItemPreview() {
    val track = TrackItem(
        id = "1",
        title = "Track Title",
        artist = "Artist Name",
        duration = "03:30",
        isPlaying = true,
        isFavorite = true,
        audioUrl = "",
        imageUrl = ""
    )
    TrackListItem(track = track, onClick = { })
}
