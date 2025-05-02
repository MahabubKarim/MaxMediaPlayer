package com.mmk.maxmediaplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mmk.maxmediaplayer.data.local.dao.PlaylistDao
import com.mmk.maxmediaplayer.data.local.dao.TrackDao
import com.mmk.maxmediaplayer.data.local.entity.PlaylistEntity
import com.mmk.maxmediaplayer.data.local.entity.PlaylistTrackJoin
import com.mmk.maxmediaplayer.data.local.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackJoin::class
    ],
    version = 3,  // Incremented version
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        /*fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "maxmediaplayer.db"
            ).addMigrations(MIGRATION_1_2)  // Add migration
                .build()
        }*/

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create playlists table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS playlists (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        coverImageUrl TEXT,
                        createdAt INTEGER NOT NULL,
                        isUserCreated INTEGER NOT NULL
                    )
                """)

                // Create junction table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS playlist_track_join (
                        playlistId TEXT NOT NULL,
                        trackId TEXT NOT NULL,
                        position INTEGER NOT NULL,
                        PRIMARY KEY (playlistId, trackId),
                        FOREIGN KEY (playlistId) REFERENCES playlists(id) ON DELETE CASCADE,
                        FOREIGN KEY (trackId) REFERENCES tracks(id) ON DELETE CASCADE
                    )
                """)

                // Create indices
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_playlist_track_join_trackId ON playlist_track_join(trackId)")
            }
        }
    }
}