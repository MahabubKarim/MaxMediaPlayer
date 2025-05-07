package com.mmk.maxmediaplayer.data.repository

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.mmk.maxmediaplayer.BuildConfig
import com.mmk.maxmediaplayer.data.local.dao.TrackDao
import com.mmk.maxmediaplayer.data.mapper.TrackMapper
import com.mmk.maxmediaplayer.data.remote.api.JamendoApi
import com.mmk.maxmediaplayer.domain.model.Playlist
import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: JamendoApi,
    private val trackDao: TrackDao
) : MusicRepository {

    override suspend fun getAllTracksOnce(): List<Track> {
        val cachedTracks = trackDao.getAllTracksOnce().map { TrackMapper.toDomain(it) }
        return cachedTracks.ifEmpty {
            fetchTracksFromNetwork()
        }
    }

    override suspend fun getTracksPaginated(page: Int, size: Int): List<Track> {
        val cachedTracks = trackDao.getTracksPaginated(page * size, size).map { TrackMapper.toDomain(it) }
        return cachedTracks.ifEmpty {
            val networkTracks = fetchTracksFromNetwork()
            networkTracks.drop(page * size).take(size)
        }
    }
    override suspend fun getNextTrack(currentTrackId: String): Track? {
        return trackDao.getNextTrack(currentTrackId)?.let { TrackMapper.toDomain(it) }
            ?: fetchTracksFromNetwork().let { tracks ->
                tracks.getOrNull(tracks.indexOfFirst { it.id == currentTrackId } + 1)
            }
    }

    override suspend fun getPreviousTrack(currentTrackId: String): Track? {
        return trackDao.getPreviousTrack(currentTrackId)?.let { TrackMapper.toDomain(it) }
            ?: fetchTracksFromNetwork().let { tracks ->
                tracks.getOrNull(tracks.indexOfFirst { it.id == currentTrackId } - 1)
            }
    }

    override suspend fun addToRecentPlays(track: Track) {
        val currentTime = System.currentTimeMillis()

        // Update or insert the track with current timestamp
        trackDao.upsertTrack(
            TrackMapper.toEntity(
                track.copy(lastPlayed = currentTime)
            )
        )
        /*trackDao.updateLastPlayed(track.id, System.currentTimeMillis())
        // Ensure the track exists in database
        if (trackDao.getTrackById(track.id) == null) {
            trackDao.insert(TrackMapper.toEntity(track))
        }*/
    }

    override suspend fun getRecentPlays(): List<Track> {
        val cachedRecent = trackDao.getRecentPlays().map { TrackMapper.toDomain(it) }
        return cachedRecent.ifEmpty {
            fetchTracksFromNetwork().sortedByDescending { it.lastPlayed }.take(10)
        }
    }

    /*override suspend fun getRecentPlays(): List<Track> {
        val cachedRecent = trackDao.getRecentPlays().map { TrackMapper.toDomain(it) }
        return cachedRecent.ifEmpty {
            fetchTracksFromNetwork().sortedByDescending { it.lastPlayed }.take(10)
        }
    }*/

    override suspend fun getFeaturedPlaylists(): List<Playlist> {
        return try {
            api.getFeaturedPlaylists(BuildConfig.CLIENT_ID)
                .body()
                ?.playlists
                ?.map { TrackMapper.toDomain(it) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrackUrl(trackId: String): String {
        return trackDao.getTrackById(trackId).audioUrl ?: ""
    }

    override suspend fun fetchTracksFromNetwork(): List<Track> {
        return try {
            // val response = api.getTracks(LocalProperties.getProperty("JAMENDO_API_KEY"))
            val response = api.getTracks(BuildConfig.CLIENT_ID)
            val tracks = response.body()?.tracks?.map { TrackMapper.toDomain(it) } ?: emptyList()
            trackDao.insertAll(tracks.map { TrackMapper.toEntity(it) })
            tracks
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getCachedTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.map { TrackMapper.toDomain(it) }
        }
    }

    override suspend fun getTracks(): List<Track> {
        val cachedTracks = trackDao.getAllTracksOnce().map { TrackMapper.toDomain(it) }
        return cachedTracks.ifEmpty { fetchTracksFromNetwork() }
    }

    override suspend fun getTrackById(trackId: String): Track? {
        val cachedTrack = trackDao.getTrackById(trackId)
        return cachedTrack.let { entity -> entity.let { TrackMapper.toDomain(it) } }
    }

    override suspend fun toggleFavorite(trackId: String) {
        trackDao.toggleFavorite(trackId)
        // Ensure track exists in database
        if (trackDao.getTrackById(trackId) == null) {
            fetchTracksFromNetwork().firstOrNull { it.id == trackId }?.let {
                trackDao.insert(TrackMapper.toEntity(it))
            }
        }
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getFavorites().map { entities ->
            entities.map { TrackMapper.toDomain(it) }
        }
    }

    /*verride suspend fun getLocalTracks(): List<Track> {
        val localTracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)
                val artist = cursor.getString(artistCol)
                val duration = cursor.getLong(durationCol)
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                localTracks.add(
                    Track(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        duration = duration,
                        audioUrl = uri.toString()
                    )
                )
            }
        }

        return localTracks
    }*/
}