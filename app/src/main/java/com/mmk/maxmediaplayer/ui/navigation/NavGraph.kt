package com.mmk.maxmediaplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import com.mmk.maxmediaplayer.ui.screen.home.HomeScreen
import com.mmk.maxmediaplayer.ui.screen.home.HomeViewModel
import com.mmk.maxmediaplayer.ui.screen.player.PlayerScreen
import com.mmk.maxmediaplayer.ui.screen.player.PlayerViewModel
import kotlinx.coroutines.launch

/**
 * Main navigation graph for the application
 * Defines all possible routes and their connections
 */
@Composable
fun NavGraph(repository: MusicRepository) {
    val navController = rememberNavController()

    NavHost(navController, "home") {
        composable("home") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val playerViewModel: PlayerViewModel = hiltViewModel()

            HomeScreen(
                onTrackClick = { trackId ->
                    homeViewModel.playTrackById(trackId, playerViewModel) {
                        navController.navigate("player")
                    }
                }
            )
        }

        composable("player") { backStackEntry ->
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}