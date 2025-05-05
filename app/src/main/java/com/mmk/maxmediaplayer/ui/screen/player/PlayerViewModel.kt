package com.mmk.maxmediaplayer.ui.screen.player

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import com.mmk.maxmediaplayer.service.PlaybackService
import com.mmk.maxmediaplayer.ui.screen.home.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

open class PlaybackState {
    object Loading : PlaybackState()
    object Ready : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Ended : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}

@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackService: PlaybackService,
    private val repository: MusicRepository
) : ViewModel() {

    // Expose these states to the UI
    var currentTrack: MutableStateFlow<Track?> = playbackService.currentTrack

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Playing)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    init {
        observePlaybackState()
    }

    private val _playbackServiceState = MutableStateFlow(PlaybackServiceState(
        currentTrack = null,
        isPlaying = false,
        playbackPosition = 0,
        playbackDuration = 0,
        bufferedPosition = 0,
        playbackState = PlaybackState.Playing
    ))
    val playbackServiceState: StateFlow<PlaybackServiceState> = _playbackServiceState.asStateFlow()

    private fun observePlaybackState() {
        viewModelScope.launch {
            combine(
                playbackService.currentTrack,
                playbackService.isPlaying,
                playbackService.playbackPosition,
                playbackService.playbackDuration
            ) { track, playing, pos, dur ->
                PlaybackServiceState(track, playing, pos, dur)
            }.collect {
                if (_playbackServiceState != null) {
                    _playbackServiceState.value = it
                }
            }
        }
    }

    fun togglePlayback() {
        viewModelScope.launch {
            when (playbackState.value) {
                /*is PlaybackState.Idle -> {
                    repository.getTracksOnce().firstOrNull()?.let {
                        playTrack(it)
                    }
                }*/

                is PlaybackState.Playing -> {
                    currentTrack.value?.let {
                        playbackService.pause()
                    } ?: run {
                        repository.getTracksOnce().firstOrNull()?.let { firstTrack ->
                            playTrack(firstTrack)
                        }
                    }
                    _playbackState.value = PlaybackState.Paused
                }

                is PlaybackState.Paused, is PlaybackState.Ready -> {
                    currentTrack.value?.let {
                        playbackService.resume()
                    } ?: run {
                        repository.getTracksOnce().firstOrNull()?.let { firstTrack ->
                            playTrack(firstTrack)
                        }
                    }
                    _playbackState.value = PlaybackState.Playing
                }

                is PlaybackState.Ended -> {
                    currentTrack.value?.let {
                        playbackService.resume()
                    } ?: run {
                        repository.getTracksOnce().firstOrNull()?.let { firstTrack ->
                            playTrack(firstTrack)
                        }
                    }
                    // Maybe restart the track or go to the next one
                    playbackService.seekTo(0)
                    _playbackState.value = PlaybackState.Playing
                }

                is PlaybackState.Error -> {
                    // Maybe reset or show UI feedback
                }
            }
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            val currentTracks = repository.getTracksOnce()
            val startIndex = currentTracks.indexOfFirst { it.id == track.id }

            if (startIndex != -1) {
                currentTrack.value = track
                playbackService.playPlaylist(currentTracks, startIndex)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        viewModelScope.launch {
            playbackService.seekTo(positionMs)
        }
    }

    fun skipNext() {
        viewModelScope.launch {
            val allTracks = repository.getTracksOnce()
            val current = currentTrack.value
            val index = allTracks.indexOfFirst { it.id == current?.id }
            if (index != -1 && index < allTracks.lastIndex) {
                playTrack(allTracks[index + 1])
            }
        }
    }

    fun skipPrevious() {
        viewModelScope.launch {
            val allTracks = repository.getTracksOnce()
            val current = currentTrack.value
            val index = allTracks.indexOfFirst { it.id == current?.id }
            if (index > 0) {
                playTrack(allTracks[index - 1])
            }
        }
    }
}