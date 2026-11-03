package com.winx.app.data.remote

import android.util.Log

class MediaRepository {

    private val api = RetrofitClient.mediaApi

    suspend fun getMediaForEntry(travelEntryId: Int): Result<List<MediaItemDto>> {
        return try {
            val media = api.getMediaForEntry(travelEntryId)

            Log.d(
                "MediaRepository",
                "Loaded ${media.size} media item(s) for entry $travelEntryId"
            )

            Result.success(media)
        } catch (exception: Exception) {

            Log.e(
                "MediaRepository",
                "Failed to load media for entry $travelEntryId",
                exception
            )

            Result.failure(exception)
        }
    }

    suspend fun createMedia(media: MediaItemDto): Result<MediaItemDto> {
        return try {
            val response = api.createMedia(media)

            if (response.isSuccessful && response.body() != null) {

                Log.d(
                    "MediaRepository",
                    "Media created successfully: ${response.body()?.id}"
                )

                Result.success(response.body()!!)
            } else {

                Log.e(
                    "MediaRepository",
                    "Failed to create media. HTTP ${response.code()}"
                )

                Result.failure(
                    Exception("Failed to create media. HTTP ${response.code()}")
                )
            }
        } catch (exception: Exception) {

            Log.e(
                "MediaRepository",
                "Error creating media",
                exception
            )

            Result.failure(exception)
        }
    }

    suspend fun deleteMedia(id: Int): Result<Unit> {
        return try {
            val response = api.deleteMedia(id)

            if (response.isSuccessful) {

                Log.d(
                    "MediaRepository",
                    "Media $id deleted successfully"
                )

                Result.success(Unit)
            } else {

                Log.e(
                    "MediaRepository",
                    "Failed to delete media $id. HTTP ${response.code()}"
                )

                Result.failure(
                    Exception("Failed to delete media. HTTP ${response.code()}")
                )
            }
        } catch (exception: Exception) {

            Log.e(
                "MediaRepository",
                "Error deleting media $id",
                exception
            )

            Result.failure(exception)
        }
    }
    /**
     * Loads media belonging to all supplied travel-entry IDs.
     * This keeps LibraryScreen free from hard-coded photo/video data.
     */
    suspend fun getMediaForEntries(travelEntryIds: List<Int>): Result<List<MediaItemDto>> {
        return try {
            val validIds = travelEntryIds.filter { it > 0 }.distinct()
            val allMedia = mutableListOf<MediaItemDto>()

            for (entryId in validIds) {
                allMedia += api.getMediaForEntry(entryId)
            }

            Log.d(
                "MediaRepository",
                "Loaded ${allMedia.size} media item(s) for ${validIds.size} entries"
            )

            Result.success(allMedia)
        } catch (exception: Exception) {
            Log.e(
                "MediaRepository",
                "Failed to load Library media",
                exception
            )
            Result.failure(exception)
        }
    }

}