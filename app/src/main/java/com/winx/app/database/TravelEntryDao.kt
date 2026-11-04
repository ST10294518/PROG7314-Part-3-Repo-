package com.winx.app.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Room operations for cached entries and offline synchronisation. */
@Dao
interface TravelEntryDao {

    @Query("SELECT * FROM travel_entries ORDER BY localId DESC")
    fun getAllEntries(): Flow<List<TravelEntryEntity>>

    @Query("SELECT * FROM travel_entries WHERE localId = :id LIMIT 1")
    suspend fun getEntryById(id: Int): TravelEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TravelEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: TravelEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: TravelEntryEntity)

    @Query("SELECT * FROM travel_entries WHERE syncStatus = 'PENDING'")
    suspend fun getPendingEntries(): List<TravelEntryEntity>

    @Query(
        "SELECT * FROM travel_entries " +
            "WHERE syncStatus = 'PENDING' AND serverId IS NULL"
    )
    suspend fun getPendingNewEntries(): List<TravelEntryEntity>

    // Retained for compatibility with existing callers.
    @Query(
        "UPDATE travel_entries SET syncStatus = 'SYNCED', serverId = :serverId " +
            "WHERE localId = :localId AND syncStatus = 'PENDING' AND serverId IS NULL"
    )
    suspend fun markAsSynced(localId: Int, serverId: Int): Int

    @Query(
        "UPDATE travel_entries SET clientId = :clientId " +
            "WHERE localId = :localId AND clientId IS NULL"
    )
    suspend fun assignClientId(localId: Int, clientId: String): Int

    @Query(
        "SELECT * FROM travel_entries " +
            "WHERE syncStatus = 'PENDING_DELETE' AND serverId IS NOT NULL"
    )
    suspend fun getPendingDeletions(): List<TravelEntryEntity>

    @Query(
        "UPDATE travel_entries SET syncStatus = 'PENDING_DELETE' " +
            "WHERE localId = :localId AND serverId IS NOT NULL"
    )
    suspend fun markForDeletion(localId: Int): Int

    @Query("SELECT * FROM travel_entries WHERE serverId = :serverId LIMIT 1")
    suspend fun getEntryByServerId(serverId: Int): TravelEntryEntity?

    @Query(
        "UPDATE travel_entries SET syncStatus = 'UPLOADING' " +
            "WHERE localId = :localId AND syncStatus = 'PENDING' AND serverId IS NULL"
    )
    suspend fun markAsUploading(localId: Int): Int

    @Query(
        "UPDATE travel_entries SET syncStatus = 'PENDING' " +
            "WHERE syncStatus = 'UPLOADING' AND serverId IS NULL"
    )
    suspend fun resetInterruptedUploads(): Int

    @Query(
        "UPDATE travel_entries SET syncStatus = 'DELETE_AFTER_UPLOAD' " +
            "WHERE localId = :localId AND syncStatus = 'UPLOADING'"
    )
    suspend fun markDeleteAfterUpload(localId: Int): Int

    @Query("SELECT * FROM travel_entries WHERE syncStatus = 'DELETE_AFTER_UPLOAD'")
    suspend fun getDeleteAfterUploadEntries(): List<TravelEntryEntity>

    @Query(
        """
        UPDATE travel_entries
        SET title = :title,
            location = :location,
            country = :country,
            date = :date,
            rating = :rating,
            notes = :notes
        WHERE localId = :localId
          AND syncStatus = 'PENDING'
          AND serverId IS NULL
        """
    )
    suspend fun updatePendingEntry(
        localId: Int,
        title: String,
        location: String,
        country: String,
        date: String,
        rating: Int,
        notes: String
    ): Int

    @Query(
        """
        DELETE FROM travel_entries
        WHERE localId = :localId
          AND syncStatus = 'PENDING'
          AND serverId IS NULL
        """
    )
    suspend fun deletePendingEntry(localId: Int): Int

    @Query(
        """
        UPDATE travel_entries
        SET syncStatus = 'SYNCED', serverId = :serverId
        WHERE localId = :localId
          AND syncStatus = 'UPLOADING'
          AND serverId IS NULL
        """
    )
    suspend fun confirmUploadedEntry(localId: Int, serverId: Int): Int

    @Query(
        """
        UPDATE travel_entries
        SET syncStatus = 'PENDING_DELETE', serverId = :serverId
        WHERE localId = :localId
          AND syncStatus = 'DELETE_AFTER_UPLOAD'
          AND serverId IS NULL
        """
    )
    suspend fun confirmUploadForDeletion(localId: Int, serverId: Int): Int

    @Query(
        """
        UPDATE travel_entries
        SET syncStatus = 'PENDING'
        WHERE localId = :localId
          AND syncStatus = 'UPLOADING'
          AND serverId IS NULL
        """
    )
    suspend fun resetInterruptedUpload(localId: Int): Int

    @Query(
        """
        SELECT * FROM travel_entries
        WHERE syncStatus = 'DELETE_AFTER_UPLOAD'
          AND serverId IS NULL
          AND clientId IS NOT NULL
        """
    )
    suspend fun getInterruptedUploadDeletions(): List<TravelEntryEntity>

    @Query(
        """
        UPDATE travel_entries
        SET syncStatus = 'PENDING_DELETE', serverId = :serverId
        WHERE localId = :localId
          AND syncStatus = 'DELETE_AFTER_UPLOAD'
          AND serverId IS NULL
          AND clientId = :clientId
        """
    )
    suspend fun recoverUploadDeletion(
        localId: Int,
        serverId: Int,
        clientId: String
    ): Int
}
