package com.mmk.maxmediaplayer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [Index("id", unique = true)]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,  // Use UUID or meaningful ID
    val name: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isUserCreated: Boolean = true
)

@Entity(
    tableName = "playlist_track_join",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trackId")]
)
data class PlaylistTrackJoin(
    val playlistId: String,
    val trackId: String,
    val position: Int  // For manual ordering
)