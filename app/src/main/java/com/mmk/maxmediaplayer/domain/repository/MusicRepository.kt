package com.mmk.maxmediaplayer.domain.repository

import com.mmk.maxmediaplayer.domain.model.Playlist
import com.mmk.maxmediaplayer.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getTracks(): List<Track>
    suspend fun getTracksPaginated(page: Int, size: Int): List<Track>
    suspend fun getNextTrack(currentTrackId: String): Track?
    suspend fun getPreviousTrack(currentTrackId: String): Track?
    suspend fun fetchTracksFromNetwork(): List<Track>
    suspend fun addToRecentPlays(track: Track)
    suspend fun getRecentPlays(): List<Track>
    suspend fun getFeaturedPlaylists(): List<Playlist>
    suspend fun toggleFavorite(trackId: String)
    suspend fun getTracksOnce(): List<Track>
    fun getFavoriteTracks(): Flow<List<Track>>
    fun getCachedTracks(): Flow<List<Track>>
    fun getTrackUrl(trackId: String): String
}