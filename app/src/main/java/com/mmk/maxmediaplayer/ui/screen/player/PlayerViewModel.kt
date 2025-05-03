package com.mmk.maxmediaplayer.ui.screen.player

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import com.mmk.maxmediaplayer.service.PlaybackService
import com.mmk.maxmediaplayer.ui.model.TrackItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val repository: MusicRepository,
    private val playbackService: PlaybackService
) : ViewModel() {

    // Existing state flows
    val currentTrack: StateFlow<Track?> = playbackService.currentTrack
    val isPlaying: StateFlow<Boolean> = playbackService.isPlaying
    val playbackPosition: StateFlow<Long> = playbackService.playbackPosition
    val playbackDuration: StateFlow<Long> = playbackService.playbackDuration
    val bufferedPosition: StateFlow<Long> = playbackService.bufferedPosition
    val playbackState: StateFlow<PlaybackState> = playbackService.playbackState

    // Missing methods that correspond to PlaybackService functionality:
    fun resume() {
        viewModelScope.launch {
            playbackService.resume()
        }
    }

    fun pause() {
        viewModelScope.launch {
            playbackService.pause()
        }
    }

    fun stop() {
        viewModelScope.launch {
            playbackService.stop()
        }
    }

    fun updateCurrentTrack(track: Track) {
        viewModelScope.launch {
            playbackService.updateCurrentTrack(track)
        }
    }

    // Existing methods remain unchanged...

    fun playTrack(track: Track) {
        viewModelScope.launch {
            playbackService.play(track)
            repository.addToRecentPlays(track)
        }
    }

    fun togglePlayback() {
        viewModelScope.launch {
            if (isPlaying.value) {
                playbackService.pause()
            } else {
                currentTrack.value?.let { playbackService.resume() }
                    ?: run { currentTrack.value?.let { playTrack(it) } }
            }
        }
    }

    fun skipNext() = viewModelScope.launch {
        currentTrack.value?.let { current ->
            repository.getNextTrack(current.id)?.let { nextTrack ->
                playTrack(nextTrack)
            } ?: run { playbackService.stop() }
        }
    }

    fun skipPrevious() = viewModelScope.launch {
        currentTrack.value?.let { current ->
            repository.getPreviousTrack(current.id)?.let { prevTrack ->
                playTrack(prevTrack)
            } ?: run { seekTo(0) }
        }
    }

    fun seekTo(position: Long) {
        viewModelScope.launch {
            playbackService.seekTo(position)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            currentTrack.value?.let { track ->
                repository.toggleFavorite(track.id)
                // Refresh current track to update favorite status
                currentTrack.value?.let { refreshed ->
                    playbackService.updateCurrentTrack(refreshed.copy())
                }
            }
        }
    }

    fun TrackItem.toTrack(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        duration = duration.toMilliseconds(),
        audioUrl = repository.getTrackUrl(id),
        imageUrl = imageUrl
    )

    private fun String.toMilliseconds(): Long {
        val parts = split(":")
        return if (parts.size == 2) {
            (parts[0].toLong() * 60 + parts[1].toLong()) * 1000
        } else 0L
    }
}

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Loading : PlaybackState()
    object Ready : PlaybackState()
    object Ended : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}