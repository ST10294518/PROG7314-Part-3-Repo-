package com.winx.app.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A durable local copy of media waiting to be uploaded to the Winx API.
 *
 * The file at [localFilePath] must be stored in app-private storage, not as a
 * transient gallery content URI. [entryClientId] links the upload to its travel
 * entry even before that entry receives a server ID.
 *
 * Uploads are processed in PENDING / UPLOADING / FAILED states. The upload
 * worker must reconcile interrupted UPLOADING rows before retrying, and the
 * server must support an idempotency key to avoid duplicates after a lost reply.
 */
@Entity(
    tableName = "pending_media_uploads",
    indices = [
        Index(value = ["entryClientId"]),
        Index(value = ["entryServerId"]),
        Index(value = ["status"]),
        Index(value = ["uploadId"], unique = true)
    ]
)
data class PendingMediaUploadEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val uploadId: String,
    val entryClientId: String?,
    val entryServerId: Int?,
    val localFilePath: String,
    val originalFileName: String,
    val mimeType: String,
    val mediaType: String,
    val fileSizeBytes: Long,
    val status: String = "PENDING",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val attemptCount: Int = 0,
    val lastError: String? = null
)
