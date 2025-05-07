package com.mmk.maxmediaplayer.domain.model

data class Track(
    var id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val audioUrl: String,
    val imageUrl: String,
    val lastPlayed: Long = 0,
    val isFavorite: Boolean = false,
)