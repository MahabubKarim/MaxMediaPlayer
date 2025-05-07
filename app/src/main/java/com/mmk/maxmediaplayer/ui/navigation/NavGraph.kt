package com.mmk.maxmediaplayer.ui.navigation

import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import com.mmk.maxmediaplayer.service.PlaybackService
import com.mmk.maxmediaplayer.ui.screen.home.HomeScreen
import com.mmk.maxmediaplayer.ui.screen.home.HomeViewModel
import com.mmk.maxmediaplayer.ui.screen.player.PlayerScreen

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

            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                onTrackClick = { track ->
                    homeViewModel.playTrackById(
                        track = track,
                        onPlayStart = { track ->
                            val intent = Intent(context, PlaybackService::class.java)
                            ContextCompat.startForegroundService(context, intent)
                        },
                        onSuccess = {
                            // navController.navigate("player")
                        }
                    )
                    homeViewModel.setCurrentTrack(track)
                }
            )
        }

        composable("player") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            PlayerScreen(
                viewModel = homeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}