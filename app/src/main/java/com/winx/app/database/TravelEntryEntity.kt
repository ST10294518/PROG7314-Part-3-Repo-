
package com.winx.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_entries")
data class TravelEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val serverId: Int? = null,
    val clientId: String? = null,
    val title: String,
    val location: String,
    val country: String,
    val date: String,
    val rating: Int,
    val notes: String,
    val syncStatus: String = "PENDING"
)
