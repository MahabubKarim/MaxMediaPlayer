package com.mmk.maxmediaplayer.domain.model

data class Playlist(
    val id: String,
    val title: String,
    val coverArtUrl: String?,
    val trackCount: Int,
    val tracks: List<Track> = emptyList()
)