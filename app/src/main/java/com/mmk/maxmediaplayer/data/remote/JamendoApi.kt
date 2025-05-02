package com.mmk.maxmediaplayer.data.remote

import com.mmk.maxmediaplayer.data.model.remote.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApi {
    @GET("tracks")
    suspend fun getTracks(
        @Query("client_id") clientId: String = "e254815f",
        @Query("format") format: String = "jsonpretty",
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse>
}
