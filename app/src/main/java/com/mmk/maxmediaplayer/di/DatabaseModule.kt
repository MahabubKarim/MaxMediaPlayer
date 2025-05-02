package com.mmk.maxmediaplayer.di

import android.content.Context
import androidx.room.Room
import com.mmk.maxmediaplayer.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "maxmediaplayer.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration() // Remove in production
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AppDatabase) = database.trackDao()

    @Provides
    @Singleton
    fun providePlaylistDao(database: AppDatabase) = database.playlistDao()
}