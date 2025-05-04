package com.mmk.maxmediaplayer.ui.screen.home

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.mmk.maxmediaplayer.domain.model.Playlist
import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import com.mmk.maxmediaplayer.service.PlaybackService
import com.mmk.maxmediaplayer.ui.model.TrackItem
import com.mmk.maxmediaplayer.ui.screen.player.PlayerViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val repository: MusicRepository,
    private val playbackService: PlaybackService
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private var currentPage = 0
    private val pageSize = 20
    private var hasMore = true

    init {
        Log.d("HomeViewModel", "Initializing with state: ${_uiState.value}")
        loadInitialData()
        setupPlaybackObserver()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val featured = repository.getFeaturedPlaylists().map(::toPlaylistItem)
                val recent = repository.getRecentPlays().map(::toTrackItem)
                val tracks = repository.getTracksPaginated(page = 0, size = pageSize).map(::toTrackItem)

                hasMore = tracks.size == pageSize

                _uiState.value = HomeUiState.Success(
                    tracks = tracks,
                    featuredPlaylists = featured,
                    recentPlays = recent,
                    hasMore = hasMore
                ).also {
                    Log.d("HomeViewModel", "New state: Success with ${tracks.size} tracks")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load data")
            }
        }
    }

    fun loadMoreTracks() {
        if (!hasMore || (_uiState.value as? HomeUiState.Success)?.isLoadingMore == true) return

        viewModelScope.launch {
            _uiState.value = (_uiState.value as HomeUiState.Success).copy(
                isLoadingMore = true
            )

            try {
                val newTracks = repository
                    .getTracksPaginated(page = ++currentPage, size = pageSize)
                    .map(::toTrackItem)

                hasMore = newTracks.size == pageSize

                _uiState.value = (_uiState.value as HomeUiState.Success).run {
                    copy(
                        tracks = tracks + newTracks,
                        hasMore = hasMore,
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                currentPage-- // Rollback page increment on failure
                _uiState.value = (_uiState.value as HomeUiState.Success).copy(
                    isLoadingMore = false
                )
            }
        }
    }

    fun refreshData() {
        currentPage = 0
        hasMore = true
        loadInitialData()
    }

    private fun setupPlaybackObserver() {
        viewModelScope.launch {
            playbackService.currentTrack.collect { currentTrack ->
                updatePlayingState(currentTrack)
            }
        }
    }

    private fun updatePlayingState(currentTrack: Track?) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(
                tracks = currentState.tracks.updatePlayingState(currentTrack),
                recentPlays = currentState.recentPlays.updatePlayingState(currentTrack)
            )
        }
    }

    private fun toTrackItem(track: Track): TrackItem = TrackItem.fromTrack(
        track = track,
        isCurrentlyPlaying = playbackService.currentTrack.value?.id == track.id
    )

    private fun toPlaylistItem(playlist: Playlist): PlaylistItem {
        return PlaylistItem(
            id = playlist.id,
            title = playlist.title,
            coverArtUrl = playlist.coverArtUrl,
            trackCount = playlist.trackCount
        )
    }

    private fun List<TrackItem>.updatePlayingState(currentTrack: Track?): List<TrackItem> {
        return map { item ->
            item.copy(isPlaying = currentTrack?.id == item.id)
        }
    }

    fun playTrackById(
        trackId: String,
        playerViewModel: PlayerViewModel,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.getTrackById(trackId)?.let { track ->
                playerViewModel.playTrack(track)
                onSuccess()
            }
        }
    }
}