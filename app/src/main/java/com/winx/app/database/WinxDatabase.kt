package com.winx.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TravelEntryEntity::class, PendingMediaUploadEntity::class],
    version = 3,
    exportSchema = false
)
abstract class WinxDatabase : RoomDatabase() {

    abstract fun travelEntryDao(): TravelEntryDao

    abstract fun pendingMediaUploadDao(): PendingMediaUploadDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE travel_entries ADD COLUMN clientId TEXT"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pending_media_uploads` (
                        `localId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `uploadId` TEXT NOT NULL,
                        `entryClientId` TEXT,
                        `entryServerId` INTEGER,
                        `localFilePath` TEXT NOT NULL,
                        `originalFileName` TEXT NOT NULL,
                        `mimeType` TEXT NOT NULL,
                        `mediaType` TEXT NOT NULL,
                        `fileSizeBytes` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        `attemptCount` INTEGER NOT NULL,
                        `lastError` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_pending_media_uploads_entryClientId` " +
                        "ON `pending_media_uploads` (`entryClientId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_pending_media_uploads_entryServerId` " +
                        "ON `pending_media_uploads` (`entryServerId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_pending_media_uploads_status` " +
                        "ON `pending_media_uploads` (`status`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_pending_media_uploads_uploadId` " +
                        "ON `pending_media_uploads` (`uploadId`)"
                )
            }
        }

        @Volatile
        private var INSTANCE: WinxDatabase? = null

        fun getDatabase(context: Context): WinxDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WinxDatabase::class.java,
                    "winx_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
