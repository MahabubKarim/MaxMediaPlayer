package com.mmk.maxmediaplayer.data.mapper

import com.mmk.maxmediaplayer.data.local.entity.TrackEntity
import com.mmk.maxmediaplayer.data.remote.dto.PlaylistDto
import com.mmk.maxmediaplayer.data.remote.dto.TrackDto
import com.mmk.maxmediaplayer.domain.model.Playlist
import com.mmk.maxmediaplayer.domain.model.Track

object TrackMapper {
    fun toDomain(dto: TrackDto): Track = Track(
        id = dto.id,
        title = dto.title,
        artist = dto.artist,
        duration = dto.duration * 1000L,
        audioUrl = dto.audioUrl,
        imageUrl = dto.imageUrl
    )

    fun toDto(domain: Track): TrackDto = TrackDto(
        id = domain.id,
        title = domain.title,
        artist = domain.artist,
        duration = (domain.duration / 1000).toInt(),
        audioUrl = domain.audioUrl,
        imageUrl = domain.imageUrl
    )

    fun toEntity(domain: Track) = TrackEntity(
        id = domain.id,
        title = domain.title,
        artist = domain.artist,
        duration = domain.duration,
        audioUrl = domain.audioUrl,
        imageUrl = domain.imageUrl,
        lastPlayed = domain.lastPlayed
    )

    fun toDomain(entity: TrackEntity) = Track(
        id = entity.id,
        title = entity.title,
        artist = entity.artist,
        duration = entity.duration,
        audioUrl = entity.audioUrl,
        imageUrl = entity.imageUrl,
        lastPlayed = entity.lastPlayed
    )

    fun toDomain(dto: PlaylistDto): Playlist {
        return Playlist(
            id = dto.id,
            title = dto.name,
            coverArtUrl = dto.imageUrl,
            trackCount = dto.trackCount
        )
    }
}