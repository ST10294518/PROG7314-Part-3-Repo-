package com.winx.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface WinxApiService {

    // GET all restaurant/travel entries
    @GET("api/Entries")
    suspend fun getEntries(): List<TravelEntryDto>

    // GET one entry by ID
    @GET("api/Entries/{id}")
    suspend fun getEntry(
        @Path("id") id: Int
    ): TravelEntryDto

    // CREATE a new entry
    @POST("api/Entries")
    suspend fun createEntry(
        @Body entry: TravelEntryDto
    ): Response<TravelEntryDto>

    // UPDATE an existing entry
    @PUT("api/Entries/{id}")
    suspend fun updateEntry(
        @Path("id") id: Int,
        @Body entry: TravelEntryDto
    ): Response<Unit>

    // DELETE an entry
    @DELETE("api/Entries/{id}")
    suspend fun deleteEntry(
        @Path("id") id: Int
    ): Response<Unit>
}
