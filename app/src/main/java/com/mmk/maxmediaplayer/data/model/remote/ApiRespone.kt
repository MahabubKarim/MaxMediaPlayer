package com.mmk.maxmediaplayer.data.model.remote

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("results") val tracks: List<TrackDto>
)