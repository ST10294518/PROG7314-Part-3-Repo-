package com.winx.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** Database operations for media files retained in app-private storage. */
@Dao
interface PendingMediaUploadDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(upload: PendingMediaUploadEntity): Long

    @Query("SELECT * FROM pending_media_uploads WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): PendingMediaUploadEntity?

    @Query("SELECT * FROM pending_media_uploads WHERE uploadId = :uploadId LIMIT 1")
    suspend fun getByUploadId(uploadId: String): PendingMediaUploadEntity?

    @Query("SELECT * FROM pending_media_uploads ORDER BY createdAtMillis ASC, localId ASC")
    suspend fun getAll(): List<PendingMediaUploadEntity>

    @Query("""
        SELECT * FROM pending_media_uploads
        WHERE status IN ('PENDING', 'FAILED')
        ORDER BY createdAtMillis ASC, localId ASC
    """)
    suspend fun getRetryableUploads(): List<PendingMediaUploadEntity>

    @Query("""
        SELECT * FROM pending_media_uploads
        WHERE (entryClientId = :clientId AND :clientId IS NOT NULL)
           OR (entryServerId = :serverId AND :serverId IS NOT NULL)
        ORDER BY createdAtMillis ASC, localId ASC
    """)
    suspend fun getForEntry(clientId: String?, serverId: Int?): List<PendingMediaUploadEntity>

    /** Claim a queued upload atomically so concurrent sync passes cannot both send it. */
    @Query("""
        UPDATE pending_media_uploads
        SET status = 'UPLOADING', attemptCount = attemptCount + 1, lastError = NULL
        WHERE localId = :localId AND status IN ('PENDING', 'FAILED')
    """)
    suspend fun claimForUpload(localId: Long): Int

    /** On process restart, a lost HTTP response must be reconciled by uploadId. */
    @Query("""
        UPDATE pending_media_uploads SET status = 'FAILED',
            lastError = 'Upload interrupted; retry requires server idempotency.'
        WHERE status = 'UPLOADING'
    """)
    suspend fun recoverInterruptedUploads(): Int

    @Query("""
        UPDATE pending_media_uploads SET status = 'FAILED', lastError = :message
        WHERE localId = :localId AND status = 'UPLOADING'
    """)
    suspend fun markFailed(localId: Long, message: String): Int

    @Query("""
        UPDATE pending_media_uploads SET status = 'PENDING', lastError = NULL
        WHERE localId = :localId AND status = 'FAILED'
    """)
    suspend fun markPending(localId: Long): Int

    @Query("""
        UPDATE pending_media_uploads SET entryServerId = :serverId
        WHERE entryClientId = :clientId AND (entryServerId IS NULL OR entryServerId != :serverId)
    """)
    suspend fun attachServerId(clientId: String, serverId: Int): Int

    /** Delete the queue record only after the backend confirms the upload. */
    @Query("DELETE FROM pending_media_uploads WHERE localId = :localId AND status = 'UPLOADING'")
    suspend fun deleteCompleted(localId: Long): Int

    @Query("DELETE FROM pending_media_uploads WHERE localId = :localId AND status != 'UPLOADING'")
    suspend fun deleteIfNotUploading(localId: Long): Int

    @Query("SELECT COUNT(*) FROM pending_media_uploads WHERE status IN ('PENDING', 'FAILED', 'UPLOADING')")
    suspend fun countOutstanding(): Int
}
