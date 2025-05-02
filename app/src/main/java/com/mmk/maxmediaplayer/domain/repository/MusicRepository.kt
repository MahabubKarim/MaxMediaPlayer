package com.mmk.maxmediaplayer.domain.repository

import com.mmk.maxmediaplayer.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getTracks(): List<Track>
    suspend fun getTracksOnce(): List<Track>
    suspend fun fetchTracksFromNetwork(): List<Track>
    suspend fun playTrack(track: Track)
    suspend fun toggleFavorite(trackId: String)
    fun getFavoriteTracks(): Flow<List<Track>>
    fun getCachedTracks(): Flow<List<Track>>

    fun pause()
    fun play()
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun seekTo(position: Long)
}