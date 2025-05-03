package com.mmk.maxmediaplayer.data.remote

import com.mmk.maxmediaplayer.data.model.remote.ApiResponse
import com.mmk.maxmediaplayer.util.LocalProperties
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApi {
    @GET("tracks")
    suspend fun getTracks(
        @Query("client_id") clientId: String = LocalProperties.getProperty("JAMENDO_API_KEY"),
        //@Query("client_id") clientId: String = BuildConfig.CLIENT_ID,
        @Query("format") format: String = "jsonpretty",
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse>
}
