package com.mmk.maxmediaplayer.ui.screen.home

import com.mmk.maxmediaplayer.ui.model.TrackItem

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val tracks: List<TrackItem>,
        val featuredPlaylists: List<PlaylistItem>,
        val recentPlays: List<TrackItem>,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}

data class PlaylistItem(
    val id: String,
    val title: String,
    val coverArtUrl: String?,
    val trackCount: Int
)