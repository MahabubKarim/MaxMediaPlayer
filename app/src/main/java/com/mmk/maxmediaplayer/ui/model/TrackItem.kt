package com.mmk.maxmediaplayer.ui.model

import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.domain.repository.MusicRepository

data class TrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String, // Formatted as "MM:SS"
    val audioUrl: String,
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val lastPlayed: Long = 0,
    val imageUrl: String
) {
    companion object {
        fun fromTrack(track: Track?, isCurrentlyPlaying: Boolean = false): TrackItem {
            return TrackItem(
                id = track?.id ?: "",
                title = track?.title ?: "",
                artist = track?.artist ?: "",
                duration = track?.duration?.toFormattedTime() ?: "",
                audioUrl = track?.audioUrl ?: "",
                isPlaying = isCurrentlyPlaying,
                isFavorite = false,
                imageUrl = ""
            )
        }
    }
}

private fun String.toMilliseconds(): Long {
    val parts = split(":")
    return when (parts.size) {
        2 -> (parts[0].toLong() * 60 + parts[1].toLong()) * 1000 // mm:ss
        3 -> (parts[0].toLong() * 3600 + parts[1].toLong() * 60 + parts[2].toLong()) * 1000 // hh:mm:ss
        else -> 0L
    }
}

private fun Long.toFormattedTime(): String {
    val minutes = (this / 60_000).toString().padStart(2, '0')
    val seconds = ((this % 60_000) / 1000).toString().padStart(2, '0')
    return "$minutes:$seconds"
}