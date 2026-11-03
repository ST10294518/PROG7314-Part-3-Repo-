package com.winx.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MediaApiService {

    @GET("api/Media/entry/{travelEntryId}")
    suspend fun getMediaForEntry(
        @Path("travelEntryId") travelEntryId: Int
    ): List<MediaItemDto>

    @GET("api/Media/{id}")
    suspend fun getMedia(
        @Path("id") id: Int
    ): MediaItemDto

    @POST("api/Media")
    suspend fun createMedia(
        @Body media: MediaItemDto
    ): Response<MediaItemDto>

    @DELETE("api/Media/{id}")
    suspend fun deleteMedia(
        @Path("id") id: Int
    ): Response<Unit>
}
