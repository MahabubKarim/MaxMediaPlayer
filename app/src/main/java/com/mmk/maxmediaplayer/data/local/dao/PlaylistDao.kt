package com.mmk.maxmediaplayer.data.local.dao

import androidx.room.*
import com.mmk.maxmediaplayer.data.local.entity.PlaylistEntity
import com.mmk.maxmediaplayer.data.local.entity.PlaylistTrackJoin
import com.mmk.maxmediaplayer.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    // Playlist CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: String): PlaylistEntity?

    // Playlist-Track relationship
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackToPlaylist(join: PlaylistTrackJoin)

    @Delete
    suspend fun removeTrackFromPlaylist(join: PlaylistTrackJoin)

    @Query("""
        SELECT COUNT(*) FROM playlist_track_join 
        WHERE playlistId = :playlistId AND trackId = :trackId
    """)
    suspend fun isTrackInPlaylist(playlistId: String, trackId: String): Int

    /*
    @Query("""
        SELECT tracks.id, tracks.title, tracks.artist, tracks.duration,
               tracks.audioUrl, tracks.imageUrl, tracks.lastPlayed, tracks.isFavorite
        FROM tracks
        INNER JOIN playlist_track_join ON tracks.id = playlist_track_join.trackId
        WHERE playlist_track_join.playlistId = :playlistId
        ORDER BY playlist_track_join.position ASC
    """)
    * */
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT * FROM tracks 
        INNER JOIN playlist_track_join ON tracks.id = playlist_track_join.trackId
        WHERE playlist_track_join.playlistId = :playlistId
        ORDER BY playlist_track_join.position ASC
    """)
    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackEntity>>

    @Query("""
        UPDATE playlist_track_join 
        SET position = position - 1 
        WHERE playlistId = :playlistId AND position > :removedPosition
    """)
    suspend fun updatePositionsAfterRemoval(playlistId: String, removedPosition: Int)

    @Query("DELETE FROM playlist_track_join WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: String)
}