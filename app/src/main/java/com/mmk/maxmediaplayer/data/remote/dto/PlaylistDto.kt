package com.mmk.maxmediaplayer.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlaylistDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("image")
    val imageUrl: String?,

    @SerializedName("tracks_count")
    val trackCount: Int,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("creationdate")
    val creationDate: String
)