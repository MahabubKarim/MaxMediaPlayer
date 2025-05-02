package com.mmk.maxmediaplayer.domain.usecase

import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import com.mmk.maxmediaplayer.domain.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Business logic for retrieving tracks with:
 * - Network error handling
 * - Caching strategy
 * - Resource state management
 */
class GetTracksUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(
        forceRefresh: Boolean = false
    ): Flow<Resource<List<Track>>> = flow {
        try {
            emit(Resource.Loading())

            val tracks = if (forceRefresh) {
                // Get fresh data and update cache
                repository.fetchTracksFromNetwork()
            } else {
                // First try cache, fallback to network
                repository.getTracks()
            }

            emit(Resource.Success(tracks))
        } catch (e: Exception) {
            emit(Resource.Error(
                message = e.message ?: "Unknown error occurred",
                data = repository.getTracksOnce()  // Fallback to cached data
            ))
        }
    }
}