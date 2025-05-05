package com.mmk.maxmediaplayer.ui.screen.home

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
        loadInitialData()
        setupPlaybackObserver()
        setupPlayPauseObserver()
    }

    private fun setupPlayPauseObserver() {
        viewModelScope.launch {
            playbackService.isPlaying.collect { isPlaying ->
                updateAllTrackItems(isPlaying)
            }
        }
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
                )
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
                currentPage--
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

    private fun toTrackItem(track: Track): TrackItem = TrackItem(
        id = track.id,
        title = track.title,
        artist = track.artist,
        duration = track.duration,
        audioUrl = track.audioUrl,
        isPlaying = false,
        isFavorite = false,
        imageUrl = "",
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

    // ✅ UPDATED: Added onPlayStart parameter to start foreground service
    fun playTrackById(
        trackId: String,
        playerViewModel: PlayerViewModel,
        onPlayStart: (Track) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val track = repository.getTrackById(trackId)
            if (track != null) {
                onPlayStart(track) // Start the service before playback
                playerViewModel.playTrack(track)
                onSuccess()
            }
        }
    }

    fun shuffleAll(
        playerViewModel: PlayerViewModel,
        onPlayStart: (Track) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Get the full list of tracks
            val allTracks = repository.getTracksOnce()
            if (allTracks.isNotEmpty()) {
                // Shuffle and play as a playlist
                val shuffled = allTracks.shuffled()
                onPlayStart(shuffled.first())
                playerViewModel.playPlaylist(shuffled, 0)
                onSuccess()
            }
        }
    }

    /**
     * Whenever playback starts or stops, flip the `isPlaying` flag
     * on every TrackItem in both lists, based on the currentTrack.
     */
    private fun updateAllTrackItems(isPlaying: Boolean) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            // if stopped, no track is playing
            val playingId = if (isPlaying) playbackService.currentTrack.value?.id else null

            _uiState.value = currentState.copy(
                tracks = currentState.tracks.map { it.copy(isPlaying = it.id == playingId) },
                recentPlays = currentState.recentPlays.map { it.copy(isPlaying = it.id == playingId) }
            )
        }
    }
}
