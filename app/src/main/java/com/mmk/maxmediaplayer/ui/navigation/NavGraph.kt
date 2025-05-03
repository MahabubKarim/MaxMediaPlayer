package com.mmk.maxmediaplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmk.maxmediaplayer.ui.screen.home.HomeScreen
import com.mmk.maxmediaplayer.ui.screen.player.PlayerScreen

/**
 * Main navigation graph for the application
 * Defines all possible routes and their connections
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Home screen destination
        composable("home") {
            HomeScreen(
                onTrackClick = { trackId ->
                    navController.navigate("player/$trackId")
                }
            )
        }

        // Player screen destination with track ID argument
        composable("player/{trackId}") {
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}