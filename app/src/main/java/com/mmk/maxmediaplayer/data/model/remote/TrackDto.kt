package com.mmk.maxmediaplayer.data.model.remote

import com.google.gson.annotations.SerializedName

data class TrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val title: String,
    @SerializedName("artist_name") val artist: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("audio") val audioUrl: String,
    @SerializedName("image") val imageUrl: String
)

/*
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
)*/
