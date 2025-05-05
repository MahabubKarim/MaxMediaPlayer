package com.mmk.maxmediaplayer.ui.navigation

import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import com.mmk.maxmediaplayer.service.PlaybackService
import com.mmk.maxmediaplayer.ui.screen.home.HomeScreen
import com.mmk.maxmediaplayer.ui.screen.home.HomeViewModel
import com.mmk.maxmediaplayer.ui.screen.player.PlayerScreen
import com.mmk.maxmediaplayer.ui.screen.player.PlayerViewModel
import kotlinx.coroutines.launch

/**
 * Main navigation graph for the application
 * Defines all possible routes and their connections
 */
@OptIn(UnstableApi::class)
@Composable
fun NavGraph(repository: MusicRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController, startDestination = "home") {
        composable("home") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val playerViewModel: PlayerViewModel = hiltViewModel()

            HomeScreen(
                onTrackClick = { trackId ->
                    homeViewModel.playTrackById(
                        trackId = trackId,
                        playerViewModel = playerViewModel,
                        onPlayStart = { track ->
                            val intent = Intent(context, PlaybackService::class.java)
                            ContextCompat.startForegroundService(context, intent)
                        },
                        onSuccess = {
                            navController.navigate("player")
                        }
                    )
                }
            )
        }

        composable("player") {
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}