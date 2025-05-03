package com.mmk.maxmediaplayer.ui.model

import com.mmk.maxmediaplayer.domain.model.Track

data class TrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String, // Formatted as "MM:SS"
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val imageUrl: String
) {
    companion object {
        fun fromTrack(track: Track, isCurrentlyPlaying: Boolean = false): TrackItem {
            return TrackItem(
                id = track.id,
                title = track.title,
                artist = track.artist,
                duration = track.duration.toFormattedTime(),
                isPlaying = isCurrentlyPlaying,
                isFavorite = false,
                imageUrl = ""
            )
        }
    }
}

private fun Long.toFormattedTime(): String {
    val minutes = (this / 60_000).toString().padStart(2, '0')
    val seconds = ((this % 60_000) / 1000).toString().padStart(2, '0')
    return "$minutes:$seconds"
}