package com.mmk.maxmediaplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val audioUrl: String,
    val imageUrl: String,
    val lastPlayed: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
)
/*fun toEntity(domain: Track) = TrackEntity(
    id = domain.id,
    title = domain.title,
    artist = domain.artist,
    duration = domain.duration,
    audioUrl = domain.audioUrl,
    imageUrl = domain.imageUrl
)

fun toDomain(entity: TrackEntity) = Track(
    id = entity.id,
    title = entity.title,
    artist = entity.artist,
    duration = entity.duration,
    audioUrl = entity.audioUrl,
    imageUrl = entity.imageUrl
)*/
