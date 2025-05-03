package com.mmk.maxmediaplayer.data.repository

import androidx.media3.exoplayer.ExoPlayer
import com.mmk.maxmediaplayer.data.local.dao.TrackDao
import com.mmk.maxmediaplayer.data.mapper.TrackMapper
import com.mmk.maxmediaplayer.data.remote.api.JamendoApi
import com.mmk.maxmediaplayer.domain.model.Playlist
import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import com.mmk.maxmediaplayer.util.LocalProperties
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val api: JamendoApi,
    private val player: ExoPlayer,
    private val trackDao: TrackDao
) : MusicRepository {

    override suspend fun getTracksOnce(): List<Track> {
        return trackDao.getAllTracksOnce().map { TrackMapper.toDomain(it) }
    }

    override suspend fun getTracksPaginated(page: Int, size: Int): List<Track> {
        return trackDao.getTracksPaginated(page * size, size).map { TrackMapper.toDomain(it) }
    }

    override suspend fun getNextTrack(currentTrackId: String): Track? {
        return trackDao.getNextTrack(currentTrackId)?.let { TrackMapper.toDomain(it) }
    }

    override suspend fun getPreviousTrack(currentTrackId: String): Track? {
        return trackDao.getPreviousTrack(currentTrackId)?.let { TrackMapper.toDomain(it) }
    }

    override suspend fun addToRecentPlays(track: Track) {
        trackDao.updateLastPlayed(track.id, System.currentTimeMillis())
    }

    override suspend fun getRecentPlays(): List<Track> {
        return trackDao.getRecentPlays().map { TrackMapper.toDomain(it) }
    }

    override suspend fun getFeaturedPlaylists(): List<Playlist> {
        return api.getFeaturedPlaylists(LocalProperties.getProperty("JAMENDO_API_KEY"))
            .body()?.playlists?.map { TrackMapper.toDomain(it) } ?: emptyList()
    }

    override fun getTrackUrl(trackId: String): String {
        return trackDao.getTrackById(trackId)?.audioUrl ?: ""
    }

    override suspend fun fetchTracksFromNetwork(): List<Track> {
        return try {
            val response = api.getTracks(LocalProperties.getProperty("JAMENDO_API_KEY"))
            val tracks = response.body()?.tracks?.map { TrackMapper.toDomain(it) } ?: emptyList()
            trackDao.insertAll(tracks.map { TrackMapper.toEntity(it) })
            tracks
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getCachedTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.map { TrackMapper.toDomain(it) }
        }
    }

    override suspend fun getTracks(): List<Track> {
        val cachedTracks = trackDao.getAllTracksOnce().map { TrackMapper.toDomain(it) }
        return cachedTracks.ifEmpty { fetchTracksFromNetwork() }
    }

    override suspend fun toggleFavorite(trackId: String) {
        trackDao.toggleFavorite(trackId)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getFavorites().map { entities ->
            entities.map { TrackMapper.toDomain(it) }
        }
    }
}