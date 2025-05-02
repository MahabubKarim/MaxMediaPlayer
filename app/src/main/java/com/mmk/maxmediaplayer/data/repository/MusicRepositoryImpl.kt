package com.mmk.maxmediaplayer.data.repository

import com.mmk.maxmediaplayer.data.local.dao.TrackDao
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.mmk.maxmediaplayer.data.mapper.TrackMapper
import com.mmk.maxmediaplayer.data.remote.JamendoApi
import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val api: JamendoApi,
    private val player: ExoPlayer,
    private val trackDao: TrackDao
) : MusicRepository {

    // Remote data source (API)
    override suspend fun fetchTracksFromNetwork(): List<Track> {
        return try {
            val response = api.getTracks()
            val tracks = response.body()?.tracks?.map { TrackMapper.toDomain(it) } ?: emptyList()

            // Cache the fresh data
            trackDao.insertAll(tracks.map { TrackMapper.toEntity(it) })
            tracks
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Local data source (Database)
    override fun getCachedTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.map { TrackMapper.toDomain(it) }
        }
    }

    // Combined strategy: First try cache, then network
    override suspend fun getTracks(): List<Track> {
        val cachedTracks = trackDao.getAllTracksOnce().map { TrackMapper.toDomain(it) }
        return cachedTracks.ifEmpty {
            fetchTracksFromNetwork()
        }
    }

    override suspend fun getTracksOnce(): List<Track> {
        return trackDao.getAllTracksOnce().map { TrackMapper.toDomain(it) }
    }

    // Player controls
    override suspend fun playTrack(track: Track) {
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(track.audioUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setArtworkUri(track.imageUrl.toUri())
                        .build()
                ).build()
        )
        player.prepare()
        player.play()

        // Update last played timestamp
        trackDao.updateLastPlayed(track.id, System.currentTimeMillis())
    }

    override fun pause() {
        if (player.isPlaying) {
            player.pause()
        }
    }

    override fun play() {
        if (!player.isPlaying) {
            player.play()
        }
    }

    override fun isPlaying(): Boolean = player.isPlaying

    override fun getCurrentPosition(): Long = player.currentPosition

    override fun getDuration(): Long = player.duration

    override fun seekTo(position: Long) {
        player.seekTo(position)
    }

    // Favorites management
    override suspend fun toggleFavorite(trackId: String) {
        trackDao.toggleFavorite(trackId)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getFavorites().map { entities ->
            entities.map { TrackMapper.toDomain(it) }
        }
    }
}