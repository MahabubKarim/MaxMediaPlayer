package com.mmk.maxmediaplayer.domain.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val audioUrl: String,
    val imageUrl: String
)