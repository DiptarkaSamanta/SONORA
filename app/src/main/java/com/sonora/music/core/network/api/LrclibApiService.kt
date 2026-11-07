package com.sonora.music.core.network.api

import com.sonora.music.core.network.model.LrclibResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LrclibApiService {

    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String? = null,
        @Query("duration") durationSeconds: Long? = null
    ): LrclibResponse

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrclibResponse>
}

