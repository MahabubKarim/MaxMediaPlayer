package com.mmk.maxmediaplayer.data.local.dao

import androidx.room.*
import com.mmk.maxmediaplayer.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    // Basic operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Query("SELECT * FROM tracks")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracksOnce(): List<TrackEntity>

    // Pagination support
    @Query("SELECT * FROM tracks LIMIT :limit OFFSET :offset")
    suspend fun getTracksPaginated(offset: Int, limit: Int): List<TrackEntity>

    // Track navigation
    @Query("SELECT * FROM tracks WHERE id > :currentTrackId ORDER BY id ASC LIMIT 1")
    suspend fun getNextTrack(currentTrackId: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE id < :currentTrackId ORDER BY id DESC LIMIT 1")
    suspend fun getPreviousTrack(currentTrackId: String): TrackEntity?

    // Recent plays
    @Query("UPDATE tracks SET lastPlayed = :timestamp WHERE id = :trackId")
    suspend fun updateLastPlayed(trackId: String, timestamp: Long)

    @Query("SELECT * FROM tracks WHERE lastPlayed > 0 ORDER BY lastPlayed DESC")
    suspend fun getRecentPlays(): List<TrackEntity>

    // Favorites
    @Query("UPDATE tracks SET isFavorite = NOT isFavorite WHERE id = :trackId")
    suspend fun toggleFavorite(trackId: String)

    @Query("SELECT * FROM tracks WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<TrackEntity>>

    // Track lookup
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    fun getTrackById(trackId: String): TrackEntity?
}