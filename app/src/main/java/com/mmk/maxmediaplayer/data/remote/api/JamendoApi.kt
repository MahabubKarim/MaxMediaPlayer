package com.mmk.maxmediaplayer.data.remote.api

import com.google.gson.annotations.SerializedName
import com.mmk.maxmediaplayer.data.remote.dto.PlaylistDto
import com.mmk.maxmediaplayer.data.remote.dto.TrackDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApi {
    @GET("tracks/")
    suspend fun getTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "jsonpretty",
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0
    ): Response<TracksResponse>

    @GET("playlists/")
    suspend fun getFeaturedPlaylists(
        @Query("client_id") clientId: String,
        @Query("featured") featured: Boolean = true,
        @Query("format") format: String = "jsonpretty",
        @Query("limit") limit: Int = 10
    ): Response<FeaturedPlaylistsResponse>
}

data class TracksResponse(
    @SerializedName("results")  val tracks: List<TrackDto>
)

data class FeaturedPlaylistsResponse(
    @SerializedName("results") val playlists: List<PlaylistDto> = emptyList()
)