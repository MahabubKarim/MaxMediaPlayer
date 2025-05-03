package com.mmk.maxmediaplayer.domain.usecase

import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.Element
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
    /**
     * In Kotlin, operator fun invoke() allows instances of a class to be called like functions.
     * When the invoke operator is defined for a class, you can call an instance of that class using
     * parentheses, similar to calling a regular function. This feature enhances code readability
     * and conciseness, and it is often used in DSL (Domain Specific Language) creation, encapsulating
     * functionality, or creating more natural syntax.
     *
     * ```
     * class Greeter(val greeting: String) {
     *     operator fun invoke(target: String) = "$greeting, $target!"
     * }
     *
     * val hello = Greeter("Hello")
     * println(hello("World")) // Output: Hello, World!]
     * ```
     *
     * In this example, hello is an instance of the Greeter class. Because the invoke operator is
     * defined, hello("World") is equivalent to hello.invoke("World"). This makes the code more
     * expressive and easier to read.
     */
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