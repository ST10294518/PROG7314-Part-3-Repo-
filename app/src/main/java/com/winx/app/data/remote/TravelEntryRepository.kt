package com.winx.app.data.remote

import android.util.Log
import com.winx.app.screens.TravelEntry

class TravelEntryRepository {

    private val api = RetrofitClient.api

    companion object {
        private const val TAG = "TravelEntryRepository"
    }

    /**
     * Retrieves all entries from the Winx REST API.
     */
    suspend fun getEntries(): Result<List<TravelEntry>> {

        return try {

            Log.d(TAG, "GET /api/Entries")

            val response = api.getEntries()

            Log.d(
                TAG,
                "GET /api/Entries successful. Entries received: ${response.size}"
            )

            Result.success(
                response.map { it.toTravelEntry() }
            )

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Failed to retrieve entries",
                exception
            )

            Result.failure(exception)
        }
    }

    /**
     * Creates a new entry through the Winx REST API.
     */
    suspend fun createEntry(entry: TravelEntry): Result<TravelEntry> {

        return try {

            Log.d(
                TAG,
                "POST /api/Entries - title=${entry.title}"
            )

            val dto = entry.toDto()

            val response = api.createEntry(dto)

            if (response.isSuccessful) {

                val createdEntry = response.body()

                if (createdEntry != null) {

                    Log.d(
                        TAG,
                        "POST /api/Entries successful. ID=${createdEntry.id}"
                    )

                    Result.success(
                        createdEntry.toTravelEntry()
                    )

                } else {

                    Log.e(
                        TAG,
                        "POST succeeded but API returned an empty response body"
                    )

                    Result.failure(
                        Exception("The API created the entry but returned no entry data.")
                    )
                }

            } else {

                Log.e(
                    TAG,
                    "POST /api/Entries failed. HTTP ${response.code()}"
                )

                Result.failure(
                    Exception(
                        "Unable to save entry. HTTP ${response.code()}"
                    )
                )
            }

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Failed to create entry",
                exception
            )

            Result.failure(exception)
        }
    }

    /**
     * Retrieves one entry by ID.
     */
    suspend fun getEntry(id: Int): Result<TravelEntry> {

        return try {

            Log.d(
                TAG,
                "GET /api/Entries/$id"
            )

            val response = api.getEntry(id)

            Log.d(
                TAG,
                "GET /api/Entries/$id successful"
            )

            Result.success(
                response.toTravelEntry()
            )

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Failed to retrieve entry ID=$id",
                exception
            )

            Result.failure(exception)
        }
    }

    /**
     * Updates an existing entry.
     */
    suspend fun updateEntry(entry: TravelEntry): Result<Unit> {

        return try {

            Log.d(
                TAG,
                "PUT /api/Entries/${entry.id}"
            )

            val response = api.updateEntry(
                entry.id,
                entry.toDto()
            )

            if (response.isSuccessful) {

                Log.d(
                    TAG,
                    "PUT /api/Entries/${entry.id} successful"
                )

                Result.success(Unit)

            } else {

                Log.e(
                    TAG,
                    "PUT failed. HTTP ${response.code()}"
                )

                Result.failure(
                    Exception(
                        "Unable to update entry. HTTP ${response.code()}"
                    )
                )
            }

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Failed to update entry ID=${entry.id}",
                exception
            )

            Result.failure(exception)
        }
    }

    /**
     * Deletes an existing entry.
     */
    suspend fun deleteEntry(id: Int): Result<Unit> {

        return try {

            Log.d(
                TAG,
                "DELETE /api/Entries/$id"
            )

            val response = api.deleteEntry(id)

            if (response.isSuccessful) {

                Log.d(
                    TAG,
                    "DELETE /api/Entries/$id successful"
                )

                Result.success(Unit)

            } else {

                Log.e(
                    TAG,
                    "DELETE failed. HTTP ${response.code()}"
                )

                Result.failure(
                    Exception(
                        "Unable to delete entry. HTTP ${response.code()}"
                    )
                )
            }

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Failed to delete entry ID=$id",
                exception
            )

            Result.failure(exception)
        }
    }

    private fun TravelEntryDto.toTravelEntry(): TravelEntry {

        return TravelEntry(
            id = id,
            title = title,
            location = location,
            country = country,
            date = date,
            rating = rating,
            notes = notes
        )
    }

    private fun TravelEntry.toDto(): TravelEntryDto {

        return TravelEntryDto(
            id = id,
            title = title,
            location = location,
            country = country,
            date = date,
            rating = rating,
            notes = notes
        )
    }
}