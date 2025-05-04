package com.mmk.maxmediaplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import android.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import androidx.core.view.WindowInsetsControllerCompat
import com.mmk.maxmediaplayer.domain.repository.MusicRepository
import com.mmk.maxmediaplayer.ui.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main entry point of the application
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: MusicRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge display
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                NavGraph(repository = repository)
            }
        }
    }

    private fun enableEdgeToEdge() {
        // Allow content to draw behind system bars
        setDecorFitsSystemWindows(window, false)

        // Configure system bar appearances
        window.run {
            // Set transparent background for system bars
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT

            // Configure system bar contrast (for icon visibility)
            /*WindowInsetsControllerCompat(this, decorView).apply {
                // Set light status bar icons (for dark themes)
                isAppearanceLightStatusBars = !isSystemInDarkTheme()
                // Set light navigation bar icons (for dark themes)
                isAppearanceLightNavigationBars = !isSystemInDarkTheme()
            }*/

            // For gesture navigation compatibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
            }
        }
    }
}