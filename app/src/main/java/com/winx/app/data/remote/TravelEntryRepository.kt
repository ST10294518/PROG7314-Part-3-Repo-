package com.winx.app.data.remote

import android.content.Context
import android.util.Log
import com.winx.app.database.TravelEntryEntity
import com.winx.app.database.WinxDatabase
import com.winx.app.screens.TravelEntry
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TravelEntryRepository(context: Context) {

    private val api = RetrofitClient.api
    private val travelEntryDao =
        WinxDatabase.getDatabase(context.applicationContext).travelEntryDao()

    companion object {
        private const val TAG = "TravelEntryRepository"

        // Repository instances created by different screens must share this lock.
        private val uploadMutex = Mutex()
        private var uploadRecoveryCompleted = false
    }

    private suspend fun cacheEntries(entries: List<TravelEntry>) {
        val existingEntries = travelEntryDao.getAllEntries().first()

        for (entry in entries) {
            val existing = existingEntries.find { it.serverId == entry.id }
                ?: entry.clientId?.let { uuid ->
                    existingEntries.find { it.clientId == uuid }
                }

            // Never overwrite unsynchronised changes or deletion tombstones.
            if (existing?.syncStatus in setOf(
                    "PENDING", "UPLOADING", "PENDING_DELETE", "DELETE_AFTER_UPLOAD"
                )
            ) continue

            travelEntryDao.insertEntry(
                TravelEntryEntity(
                    localId = existing?.localId ?: 0,
                    serverId = entry.id,
                    clientId = entry.clientId ?: existing?.clientId,
                    title = entry.title,
                    location = entry.location,
                    country = entry.country,
                    date = entry.date,
                    rating = entry.rating,
                    notes = entry.notes,
                    syncStatus = "SYNCED"
                )
            )
        }
    }

    suspend fun getEntries(): Result<List<TravelEntry>> {
        return try {
            Log.d(TAG, "GET /api/Entries")
            val downloaded = api.getEntries().map { it.toTravelEntry() }
            cacheEntries(downloaded)
            Result.success(getCachedEntries())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.w(TAG, "Unable to load entries from API; trying RoomDB", exception)
            try {
                val cached = getCachedEntries()
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(exception)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (databaseException: Exception) {
                Log.e(TAG, "Unable to read cached entries", databaseException)
                Result.failure(databaseException)
            }
        }
    }

    suspend fun createEntryOffline(entry: TravelEntry): Result<TravelEntry> {
        return try {
            val clientId = entry.clientId ?: UUID.randomUUID().toString()
            val localId = travelEntryDao.insertEntry(
                TravelEntryEntity(
                    serverId = null,
                    clientId = clientId,
                    title = entry.title,
                    location = entry.location,
                    country = entry.country,
                    date = entry.date,
                    rating = entry.rating,
                    notes = entry.notes,
                    syncStatus = "PENDING"
                )
            )
            Result.success(entry.copy(id = -localId.toInt(), clientId = clientId))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to save entry offline", exception)
            Result.failure(exception)
        }
    }

    /** Retained for callers that explicitly create entries through the API. */
    suspend fun createEntry(entry: TravelEntry): Result<TravelEntry> {
        return try {
            val response = api.createEntry(entry.toDto())
            val created = response.body()
            if (response.isSuccessful && created != null) {
                Result.success(created.toTravelEntry())
            } else {
                Result.failure(Exception("Unable to create entry. HTTP ${response.code()}"))
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to create entry", exception)
            Result.failure(exception)
        }
    }

    /** Return true only if an uploaded entry becomes visible as SYNCED. */
    private suspend fun confirmUploadState(localId: Int, serverId: Int): Boolean {
        if (travelEntryDao.confirmUploadedEntry(localId, serverId) > 0) return true
        if (travelEntryDao.confirmUploadForDeletion(localId, serverId) > 0) {
            Log.d(TAG, "Upload confirmed for $localId; deletion remains pending")
            return false
        }
        Log.w(TAG, "Upload confirmation skipped for $localId; local state changed")
        return false
    }

    /**
     * Resolve deletion tombstones by UUID. If lookup returns 404, an earlier
     * POST might still have been in flight. Repeating that POST with the same
     * UUID is safe because the backend treats clientId as an idempotency key.
     * Never discard the tombstone unless the server ID has been confirmed.
     * Must be called while holding uploadMutex.
     */
    private suspend fun recoverInterruptedUploadDeletions() {
        for (entry in travelEntryDao.getInterruptedUploadDeletions()) {
            val clientId = entry.clientId ?: continue
            try {
                val lookup = api.getEntryByClientId(clientId)
                val confirmed = when {
                    lookup.isSuccessful -> lookup.body()
                    lookup.code() == 404 -> {
                        val upload = api.createEntry(
                            TravelEntryDto(
                                title = entry.title,
                                location = entry.location,
                                country = entry.country,
                                date = entry.date,
                                rating = entry.rating,
                                notes = entry.notes,
                                clientId = clientId
                            )
                        )
                        if (upload.isSuccessful) upload.body() else {
                            Log.w(TAG, "Deletion reconciliation POST HTTP ${upload.code()}")
                            null
                        }
                    }
                    else -> {
                        Log.w(TAG, "Deletion lookup HTTP ${lookup.code()} for $clientId")
                        null
                    }
                }

                if (confirmed != null && confirmed.id > 0 &&
                    confirmed.clientId == clientId
                ) {
                    travelEntryDao.recoverUploadDeletion(
                        localId = entry.localId,
                        serverId = confirmed.id,
                        clientId = clientId
                    )
                } else {
                    Log.w(TAG, "Deletion tombstone remains unresolved for $clientId")
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.w(TAG, "Deletion recovery postponed for $clientId", exception)
            }
        }
    }

    /** Restore interrupted UPLOADING rows once per app process. */
    private suspend fun recoverInterruptedUploads() {
        uploadMutex.withLock {
            if (!uploadRecoveryCompleted) {
                val recovered = travelEntryDao.resetInterruptedUploads()
                Log.d(TAG, "Recovered $recovered interrupted uploads")
                uploadRecoveryCompleted = true
            }
        }
    }

    suspend fun syncPendingNewEntries(): Result<Int> {
        try {
            recoverInterruptedUploads()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            return Result.failure(exception)
        }

        return uploadMutex.withLock {
            try {
                // Retry deletion reconciliation on every sync, not just startup.
                recoverInterruptedUploadDeletions()
                var syncedCount = 0

                for (queued in travelEntryDao.getPendingNewEntries()) {
                    val localId = queued.localId
                    try {
                        val current = travelEntryDao.getEntryById(localId)
                        if (current == null || current.syncStatus != "PENDING" ||
                            current.serverId != null
                        ) continue

                        var clientId = current.clientId
                        if (clientId == null) {
                            val candidate = UUID.randomUUID().toString()
                            val assigned = travelEntryDao.assignClientId(localId, candidate)
                            clientId = if (assigned > 0) candidate
                            else travelEntryDao.getEntryById(localId)?.clientId
                        }
                        if (clientId == null) continue

                        // Claim the entry atomically before any network operation.
                        if (travelEntryDao.markAsUploading(localId) == 0) continue

                        val lookup = api.getEntryByClientId(clientId)
                        val confirmed = when {
                            lookup.isSuccessful -> lookup.body()
                            lookup.code() == 404 -> {
                                val response = api.createEntry(
                                    TravelEntryDto(
                                        title = current.title,
                                        location = current.location,
                                        country = current.country,
                                        date = current.date,
                                        rating = current.rating,
                                        notes = current.notes,
                                        clientId = clientId
                                    )
                                )
                                if (response.isSuccessful) response.body() else {
                                    Log.w(TAG, "Upload HTTP ${response.code()} for $localId")
                                    null
                                }
                            }
                            else -> {
                                Log.w(TAG, "UUID lookup HTTP ${lookup.code()} for $localId")
                                null
                            }
                        }

                        if (confirmed != null && confirmed.id > 0 &&
                            confirmed.clientId == clientId
                        ) {
                            if (confirmUploadState(localId, confirmed.id)) syncedCount++
                        } else {
                            Log.w(TAG, "Upload unconfirmed for $localId; retry later")
                        }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        Log.w(TAG, "Upload failed for $localId", exception)
                    } finally {
                        // Conditional update leaves DELETE_AFTER_UPLOAD untouched.
                        withContext(NonCancellable) {
                            travelEntryDao.resetInterruptedUpload(localId)
                        }
                    }
                }
                Result.success(syncedCount)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to synchronise pending entries", exception)
                Result.failure(exception)
            }
        }
    }

    suspend fun getEntry(id: Int): Result<TravelEntry> {
        if (id < 0) {
            return try {
                val entry = travelEntryDao.getEntryById(-id)
                if (entry == null || entry.syncStatus == "PENDING_DELETE" ||
                    entry.syncStatus == "DELETE_AFTER_UPLOAD"
                ) Result.failure(Exception("Entry not found"))
                else Result.success(entry.toTravelEntry())
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }
        return try {
            Result.success(api.getEntry(id).toTravelEntry())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to retrieve entry ID=$id", exception)
            Result.failure(exception)
        }
    }

    private suspend fun getCachedEntries(): List<TravelEntry> =
        travelEntryDao.getAllEntries().first()
            .filter {
                it.syncStatus != "PENDING_DELETE" &&
                    it.syncStatus != "DELETE_AFTER_UPLOAD"
            }
            .map { it.toTravelEntry() }

    suspend fun updateEntry(entry: TravelEntry): Result<Unit> {
        return try {
            if (entry.id < 0) {
                val localId = -entry.id
                val changed = travelEntryDao.updatePendingEntry(
                    localId = localId,
                    title = entry.title,
                    location = entry.location,
                    country = entry.country,
                    date = entry.date,
                    rating = entry.rating,
                    notes = entry.notes
                )
                if (changed > 0) Result.success(Unit)
                else Result.failure(
                    Exception("Entry is uploading or no longer editable offline. Refresh and retry.")
                )
            } else {
                val response = api.updateEntry(entry.id, entry.toDto())
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Unable to update entry. HTTP ${response.code()}"))
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to update entry ID=${entry.id}", exception)
            Result.failure(exception)
        }
    }

    suspend fun syncPendingDeletions(): Result<Int> {
        return try {
            var deletedCount = 0
            for (entry in travelEntryDao.getPendingDeletions()) {
                val serverId = entry.serverId ?: continue
                try {
                    val response = api.deleteEntry(serverId)
                    if (response.isSuccessful || response.code() == 404) {
                        // Only remove a row if it is still pending deletion.
                        val latest = travelEntryDao.getEntryById(entry.localId)
                        if (latest?.syncStatus == "PENDING_DELETE" &&
                            latest.serverId == serverId
                        ) {
                            travelEntryDao.deleteEntry(latest)
                            deletedCount++
                        }
                    } else {
                        Log.w(TAG, "Deletion HTTP ${response.code()} for $serverId")
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    Log.w(TAG, "Unable to delete server entry $serverId", exception)
                }
            }
            Result.success(deletedCount)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to synchronise deletions", exception)
            Result.failure(exception)
        }
    }

    suspend fun deleteEntry(id: Int): Result<Unit> {
        return try {
            if (id < 0) {
                val localId = -id
                val existing = travelEntryDao.getEntryById(localId)
                when {
                    existing == null || existing.serverId != null ->
                        Result.failure(Exception("Offline entry not found. Refresh and retry."))
                    existing.syncStatus == "UPLOADING" -> {
                        if (travelEntryDao.markDeleteAfterUpload(localId) > 0)
                            Result.success(Unit)
                        else Result.failure(Exception("Entry state changed. Refresh and retry."))
                    }
                    existing.syncStatus == "PENDING" -> {
                        if (travelEntryDao.deletePendingEntry(localId) > 0 ||
                            travelEntryDao.markDeleteAfterUpload(localId) > 0
                        ) Result.success(Unit)
                        else Result.failure(Exception("Entry state changed. Refresh and retry."))
                    }
                    else -> Result.failure(Exception("Entry is already being deleted."))
                }
            } else {
                val cached = travelEntryDao.getEntryByServerId(id)
                if (cached != null) {
                    if (cached.syncStatus == "PENDING_DELETE" ||
                        travelEntryDao.markForDeletion(cached.localId) > 0
                    ) Result.success(Unit)
                    else Result.failure(Exception("Unable to mark entry for deletion."))
                } else {
                    val response = api.deleteEntry(id)
                    if (response.isSuccessful || response.code() == 404)
                        Result.success(Unit)
                    else Result.failure(Exception("Unable to delete entry. HTTP ${response.code()}"))
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to delete entry ID=$id", exception)
            Result.failure(exception)
        }
    }

    private fun TravelEntryEntity.toTravelEntry() = TravelEntry(
        id = serverId ?: -localId,
        title = title,
        location = location,
        country = country,
        date = date,
        rating = rating,
        notes = notes,
        clientId = clientId
    )

    private fun TravelEntryDto.toTravelEntry() = TravelEntry(
        id = id,
        title = title,
        location = location,
        country = country,
        date = date,
        rating = rating,
        notes = notes,
        clientId = clientId
    )

    private fun TravelEntry.toDto() = TravelEntryDto(
        id = id,
        title = title,
        location = location,
        country = country,
        date = date,
        rating = rating,
        notes = notes,
        clientId = clientId
    )
}
