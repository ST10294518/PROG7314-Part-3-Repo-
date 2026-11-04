package com.winx.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    // Retained for compatibility with the existing online upload flow.
    @Multipart
    @POST("api/Media/upload")
    suspend fun uploadMedia(
        @Part("travelEntryId") travelEntryId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<MediaItemDto>

    // Offline queue uploads use a stable UUID for safe retries.
    @Multipart
    @POST("api/Media/upload")
    suspend fun uploadMediaWithId(
        @Part("travelEntryId") travelEntryId: RequestBody,
        @Part("uploadId") uploadId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<MediaItemDto>

    // Check whether the server already saved an upload whose response was lost.
    // A 404 means that uploadId has not been found on the server.
    @GET("api/Media/upload/{uploadId}")
    suspend fun getMediaByUploadId(
        @Path("uploadId") uploadId: String
    ): Response<MediaItemDto>

    @DELETE("api/Media/{id}")
    suspend fun deleteMedia(
        @Path("id") id: Int
    ): Response<Unit>
}
