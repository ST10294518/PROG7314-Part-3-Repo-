package com.winx.app.data.remote

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.winx.app.database.PendingMediaUploadEntity
import com.winx.app.database.WinxDatabase
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class MediaRepository {

    private val api = RetrofitClient.mediaApi

    suspend fun getMediaForEntry(travelEntryId: Int): Result<List<MediaItemDto>> {
        return try {
            val media = api.getMediaForEntry(travelEntryId)
            Log.d(TAG, "Loaded ${media.size} media item(s) for entry $travelEntryId")
            Result.success(media)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to load media for entry $travelEntryId", exception)
            Result.failure(exception)
        }
    }

    suspend fun createMedia(media: MediaItemDto): Result<MediaItemDto> {
        return try {
            val response = api.createMedia(media)
            val createdMedia = response.body()
            if (response.isSuccessful && createdMedia != null) {
                Log.d(TAG, "Media created successfully: ${createdMedia.id}")
                Result.success(createdMedia)
            } else {
                Result.failure(IOException("Failed to create media. HTTP ${response.code()}"))
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Error creating media", exception)
            Result.failure(exception)
        }
    }

    suspend fun deleteMedia(id: Int): Result<Unit> {
        return try {
            val response = api.deleteMedia(id)
            if (response.isSuccessful) {
                Log.d(TAG, "Media $id deleted successfully")
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to delete media. HTTP ${response.code()}"))
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Error deleting media $id", exception)
            Result.failure(exception)
        }
    }

    /** Loads media for server-side entries; local-only entries have no server media yet. */
    suspend fun getMediaForEntries(travelEntryIds: List<Int>): Result<List<MediaItemDto>> {
        return try {
            val validIds = travelEntryIds.filter { it > 0 }.distinct()
            val allMedia = mutableListOf<MediaItemDto>()
            for (entryId in validIds) {
                allMedia += api.getMediaForEntry(entryId)
            }
            Log.d(TAG, "Loaded ${allMedia.size} media item(s) for ${validIds.size} entries")
            Result.success(allMedia)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to load Library media", exception)
            Result.failure(exception)
        }
    }

    /**
     * Copies gallery media into durable app-private storage and records a Room
     * upload job. This does not send the file over the network: the sync step
     * must wait until the travel entry has a confirmed server ID and the API
     * supports uploadId idempotency.
     *
     * Supply entryClientId for offline entries; supply entryServerId for
     * server-only entries. Both can be supplied when available.
     */
    suspend fun queueMediaUpload(
        context: Context,
        uri: Uri,
        entryClientId: String? = null,
        entryServerId: Int? = null
    ): Result<PendingMediaUploadEntity> = withContext(Dispatchers.IO) {
        var storedFile: File? = null
        try {
            require(!entryClientId.isNullOrBlank() || (entryServerId ?: 0) > 0) {
                "A travel entry client ID or server ID is required."
            }
            require(entryServerId == null || entryServerId > 0) {
                "Server entry ID must be positive."
            }

            val info = inspectMedia(context, uri)
            val directory = File(context.applicationContext.filesDir, "pending_media_uploads")
            if (!directory.isDirectory && !directory.mkdirs()) {
                throw IOException("Unable to create private media storage.")
            }

            val uploadId = UUID.randomUUID().toString()
            val file = File(directory, "$uploadId.${info.extension}")
            storedFile = file
            copyUriToFile(context, uri, file, info.maxBytes)

            val record = PendingMediaUploadEntity(
                uploadId = uploadId,
                entryClientId = entryClientId?.takeIf { it.isNotBlank() },
                entryServerId = entryServerId,
                localFilePath = file.absolutePath,
                originalFileName = info.fileName,
                mimeType = info.mimeType,
                mediaType = if (info.isVideo) "Video" else "Photo",
                fileSizeBytes = file.length()
            )
            WinxDatabase.getDatabase(context.applicationContext)
                .pendingMediaUploadDao().insert(record)
            storedFile = null // Room now owns the durable file's lifecycle.
            Log.d(TAG, "Queued offline media $uploadId")
            Result.success(record)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to queue media upload", exception)
            Result.failure(exception)
        } finally {
            // Never leave an orphan file if the copy or database insert fails.
            storedFile?.delete()
        }
    }

    /** Read pending uploads for a travel entry, including offline-only entries. */
    suspend fun getQueuedMediaForEntry(
        context: Context,
        entryClientId: String?,
        entryServerId: Int?
    ): Result<List<PendingMediaUploadEntity>> = withContext(Dispatchers.IO) {
        try {
            Result.success(
                WinxDatabase.getDatabase(context.applicationContext)
                    .pendingMediaUploadDao().getForEntry(entryClientId, entryServerId)
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    /**
     * Remove a queued item that is not currently uploading. The local file is
     * removed only after the database confirms that the queue row was deleted.
     */
    suspend fun removeQueuedMedia(
        context: Context,
        localId: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dao = WinxDatabase.getDatabase(context.applicationContext).pendingMediaUploadDao()
            val record = dao.getByLocalId(localId)
                ?: return@withContext Result.failure(IOException("Queued media not found."))
            if (dao.deleteIfNotUploading(localId) == 0) {
                return@withContext Result.failure(IOException("Media is currently uploading."))
            }
            val directory = File(context.applicationContext.filesDir, "pending_media_uploads")
            val file = File(record.localFilePath)
            if (file.parentFile?.canonicalPath == directory.canonicalPath && file.exists() && !file.delete()) {
                Log.w(TAG, "Unable to delete local queued file ${record.uploadId}")
            }
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    /**
     * Reconcile and send durable uploads. Invoke after travel-entry synchronization.
     * A network failure stops this pass; remaining rows stay queued for a later retry.
     * This method does not schedule itself when connectivity returns.
     *
     * Returns the number of uploads confirmed by the server in this pass.
     */
    suspend fun syncPendingMediaUploads(context: Context): Result<Int> = withContext(Dispatchers.IO) {
        syncMutex.withLock {
        val appContext = context.applicationContext
        val database = WinxDatabase.getDatabase(appContext)
        val queue = database.pendingMediaUploadDao()
        val entries = database.travelEntryDao()
        var completed = 0

        try {
            // Only one sync pass should run at a time within this repository instance.
            // The DAO claim also protects against concurrent passes across instances.
            queue.recoverInterruptedUploads()
            val candidates = queue.getRetryableUploads()
            for (record in candidates) {
                coroutineContext.ensureActive()

                // Resolve offline entries by their stable client ID, not their temporary ID.
                val entry = record.entryClientId?.let { clientId ->
                    entries.getAllEntries().first().firstOrNull { it.clientId == clientId }
                } ?: record.entryServerId?.let { entries.getEntryByServerId(it) }

                // Do not upload media belonging to a travel entry being deleted.
                if (entry?.syncStatus in setOf("PENDING_DELETE", "DELETE_AFTER_UPLOAD")) continue
                val serverId = entry?.serverId ?: record.entryServerId
                if (serverId == null || serverId <= 0) continue

                if (queue.claimForUpload(record.localId) != 1) continue
                try {
                    // Check before resending: the previous HTTP response may have been lost.
                    val existing = api.getMediaByUploadId(record.uploadId)
                    when {
                        existing.isSuccessful && existing.body() != null -> {
                            if (existing.body()!!.travelEntryId != serverId) {
                                throw IOException("Upload ID belongs to a different travel entry.")
                            }
                        }
                        existing.code() == 404 -> {
                            val directory = File(appContext.filesDir, "pending_media_uploads")
                            val file = File(record.localFilePath)
                            if (file.parentFile?.canonicalPath != directory.canonicalPath || !file.isFile) {
                                throw IOException("Queued media file is missing or outside private storage.")
                            }
                            if (file.length() != record.fileSizeBytes) {
                                throw IOException("Queued media file size has changed.")
                            }
                            val filePart = MultipartBody.Part.createFormData(
                                "file", "upload.${file.extension}",
                                file.asRequestBody(record.mimeType.toMediaTypeOrNull())
                            )
                            val response = api.uploadMediaWithId(
                                serverId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                                record.uploadId.toRequestBody("text/plain".toMediaTypeOrNull()),
                                filePart
                            )
                            if (!response.isSuccessful || response.body() == null) {
                                throw IOException("Media upload failed. HTTP ${response.code()}")
                            }
                            if (response.body()!!.travelEntryId != serverId) {
                                throw IOException("Server returned media for a different entry.")
                            }
                        }
                        else -> throw IOException("Upload reconciliation failed. HTTP ${existing.code()}")
                    }

                    if (queue.deleteCompleted(record.localId) == 1) {
                        val directory = File(appContext.filesDir, "pending_media_uploads")
                        val file = File(record.localFilePath)
                        if (file.parentFile?.canonicalPath == directory.canonicalPath && file.exists() && !file.delete()) {
                            Log.w(TAG, "Could not remove confirmed upload file ${record.uploadId}")
                        }
                        completed++
                    }
                } catch (exception: CancellationException) {
                    queue.markFailed(record.localId, "Synchronization interrupted")
                    throw exception
                } catch (exception: Exception) {
                    queue.markFailed(record.localId, exception.message ?: "Upload failed")
                    Log.e(TAG, "Failed to synchronize media ${record.uploadId}", exception)
                    // Avoid repeatedly attempting uploads when the network or API is down.
                    return@withLock Result.failure(exception)
                }
            }
            Result.success(completed)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Media synchronization failed", exception)
            Result.failure(exception)
        }
    }

    }

    /** Existing immediate online upload, retained for current screen callers. */
    suspend fun uploadMedia(
        context: Context,
        travelEntryId: Int,
        uri: Uri
    ): Result<MediaItemDto> = withContext(Dispatchers.IO) {
        var temporaryFile: File? = null
        try {
            require(travelEntryId > 0) { "A synced travel entry ID is required to upload media." }
            val info = inspectMedia(context, uri)
            val file = File.createTempFile("winx_upload_", ".${info.extension}", context.cacheDir)
            temporaryFile = file
            copyUriToFile(context, uri, file, info.maxBytes)

            val filePart = MultipartBody.Part.createFormData(
                "file", "upload.${info.extension}",
                file.asRequestBody(info.mimeType.toMediaTypeOrNull())
            )
            val idPart = travelEntryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val response = api.uploadMedia(idPart, filePart)
            val uploaded = response.body()
            if (response.isSuccessful && uploaded != null) {
                Log.d(TAG, "Uploaded media ${uploaded.id} for entry $travelEntryId")
                Result.success(uploaded)
            } else {
                Result.failure(IOException("Media upload failed. HTTP ${response.code()}"))
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to upload media for entry $travelEntryId", exception)
            Result.failure(exception)
        } finally {
            temporaryFile?.delete()
        }
    }

    private data class MediaInfo(
        val extension: String,
        val mimeType: String,
        val fileName: String,
        val isVideo: Boolean,
        val maxBytes: Long
    )

    private fun inspectMedia(context: Context, uri: Uri): MediaInfo {
        val resolver = context.contentResolver
        var displayName: String? = null
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) displayName = cursor.getString(index)
            }
        }
        val sourceMime = resolver.getType(uri)?.lowercase()
        val extension = displayName?.substringAfterLast('.', "")?.lowercase().orEmpty()
        val mimeExtension = when (sourceMime) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "video/mp4" -> "mp4"
            "video/quicktime" -> "mov"
            "video/webm" -> "webm"
            else -> null
        }
        val supported = setOf("jpg", "jpeg", "png", "gif", "webp", "mp4", "mov", "webm")
        val safeExtension = extension.takeIf { it in supported } ?: mimeExtension
        require(safeExtension != null) {
            "Supported formats: JPG, PNG, GIF, WEBP, MP4, MOV and WEBM."
        }
        val isVideo = safeExtension in setOf("mp4", "mov", "webm")
        val mimeType = when (safeExtension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            else -> "video/webm"
        }
        return MediaInfo(
            extension = safeExtension,
            mimeType = mimeType,
            fileName = displayName?.takeIf { it.isNotBlank() } ?: "media.$safeExtension",
            isVideo = isVideo,
            maxBytes = if (isVideo) 100L * 1024 * 1024 else 10L * 1024 * 1024
        )
    }

    private suspend fun copyUriToFile(context: Context, uri: Uri, file: File, maxBytes: Long) {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Cannot open selected media.")
        input.use { source ->
            file.outputStream().use { destination ->
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    coroutineContext.ensureActive()
                    val count = source.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > maxBytes) {
                        throw IOException("Selected media exceeds the upload size limit.")
                    }
                    destination.write(buffer, 0, count)
                }
                if (total == 0L) throw IOException("Selected media file is empty.")
            }
        }
    }

    companion object {
        private const val TAG = "MediaRepository"
        private val syncMutex = Mutex()
    }
}
