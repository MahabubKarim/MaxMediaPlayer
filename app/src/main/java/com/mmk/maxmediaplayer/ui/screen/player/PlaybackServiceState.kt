package com.mmk.maxmediaplayer.ui.screen.player

import com.mmk.maxmediaplayer.domain.model.Track

// Data class to hold the playback state information
data class PlaybackServiceState(
    val currentTrack: Track?,
    val isPlaying: Boolean = false,
    val playbackPosition: Long = 0L,
    val playbackDuration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val playbackState: PlaybackState = PlaybackState.Playing
)