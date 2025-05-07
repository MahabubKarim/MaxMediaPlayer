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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val repository: MusicRepository,
    private val playbackService: PlaybackService
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    val currentTrackId: StateFlow<String?> = _currentTrack
        .map { it?.id } // convert Track to track ID
        .stateIn(       // convert Flow to StateFlow
            viewModelScope,                      // runs in ViewModel coroutine scope
            SharingStarted.WhileSubscribed(5000),// keep it alive while collected, with 5s delay
            null                                 // initial value if nothing is emitted yet
        )

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration.asStateFlow()


    private var currentPage = 0
    private val pageSize = 20
    private var hasMore = true

    init {
        loadInitialData()
        viewModelScope.launch {
            playbackService.currentTrack.collect { track ->
                _currentTrack.value = track
            }
        }
        viewModelScope.launch {
            playbackService.isPlaying.collect { playing ->
                _isPlaying.value = playing
            }
        }
        viewModelScope.launch {
            playbackService.playbackPosition.collect { position ->
                _playbackPosition.value = position
            }
        }
        viewModelScope.launch {
            playbackService.playbackDuration.collect { duration ->
                _playbackDuration.value = duration
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val featured = repository.getFeaturedPlaylists().map(::toPlaylistItem)
                val recent = repository.getRecentPlays()
                // val recent = repository.getRecentPlays().map(::toTrackItem)
                val tracks = repository.getTracksPaginated(page = 0, size = pageSize)
                // val tracks = repository.getTracksPaginated(page = 0, size = pageSize).map(::toTrackItem)

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
                    //.map(::toTrackItem)

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

    fun setCurrentTrack(track: Track) {
        _currentTrack.value = track
    }

    @OptIn(UnstableApi::class)
    fun playTrack(track: Track) = viewModelScope.launch {
        // Fetch the full list and find the index of the requested track
        val allTracks = repository.getAllTracksOnce()
        val startIndex = allTracks.indexOfFirst { it.id == track.id }

        if (startIndex != -1) {
            // Let the service load the playlist and update its own state
            playbackService.playPlaylist(allTracks, startIndex)
        } else {
            // Optionally handle the error case here, e.g.:
            // Log.e("PlayerViewModel", "Track not found: ${track.id}")
        }
    }

    @OptIn(UnstableApi::class)
    fun playTrackById(
        track: Track,
        onPlayStart: (Track) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (track.id != currentTrack.value?.id) {
                // If a new track is selected
                _currentTrack.value = track
                onPlayStart(track)
                playTrack(track)
                onSuccess()
            } else {
                // Toggle play/pause for the same track
                if (_isPlaying.value) {
                    playbackService.pause()
                } else {
                    onPlayStart(track)
                    playTrack(track)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun togglePlayback() = viewModelScope.launch {
        if (currentTrack.value == null) {
            // no track loaded yet, fetch & play first one
            repository.getAllTracksOnce().firstOrNull()?.let {
                playbackService.playPlaylist(listOf(it), 0)
            }
        } else {
            // just flip play/pause
            if (_isPlaying.value) playbackService.pause()
            else playbackService.play(currentTrack.value!!)
        }
        _isPlaying.value = !_isPlaying.value
    }

    fun skipNext() {
        viewModelScope.launch {
            val allTracks = repository.getAllTracksOnce()
            val current = currentTrack.value
            val index = allTracks.indexOfFirst { it.id == current?.id }
            if (index != -1 && index < allTracks.lastIndex) {
                playTrack(allTracks[index + 1])
            }
        }
    }

    fun skipPrevious() {
        viewModelScope.launch {
            val allTracks = repository.getAllTracksOnce()
            val current = currentTrack.value
            val index = allTracks.indexOfFirst { it.id == current?.id }
            if (index > 0) {
                playTrack(allTracks[index - 1])
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun seekTo(positionMs: Long) {
        viewModelScope.launch {
            playbackService.seekTo(positionMs)
        }
    }

    /*fun shuffleAll(
        playerViewModel: PlayerViewModel,
        onPlayStart: (Track) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Get the full list of tracks
            val allTracks = repository.getAllTracksOnce()
            if (allTracks.isNotEmpty()) {
                // Shuffle and play as a playlist
                val shuffled = allTracks.shuffled()
                onPlayStart(shuffled.first())
                playerViewModel.playPlaylist(shuffled, 0)
                onSuccess()
            }
        }
    }*/

    /*fun togglePlayback() {
        val currentlyPlaying = _isPlaying.value
        if (currentlyPlaying) {
            // pause player
        } else {
            // play player
        }
        _isPlaying.value = !currentlyPlaying
    }*/

}
