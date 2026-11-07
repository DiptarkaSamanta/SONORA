package com.sonora.music.core.network.api

import com.sonora.music.core.network.model.JamendoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {

    @GET("v3.0/tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String = "c89e0ee2", // Public developer client ID for Jamendo CC discovery
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 25,
        @Query("namesearch") query: String? = null,
        @Query("artist_name") artistName: String? = null,
        @Query("order") order: String = "popularity_total",
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoResponse

    @GET("v3.0/tracks/")
    suspend fun getTrendingTracks(
        @Query("client_id") clientId: String = "c89e0ee2",
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("order") order: String = "popularity_week",
        @Query("featured") featured: Int = 1
    ): JamendoResponse
}

