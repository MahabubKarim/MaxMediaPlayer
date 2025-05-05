package com.mmk.maxmediaplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary       = NeonGreen,
    onPrimary     = DarkBackground,
    secondary     = Aqua,
    onSecondary   = DarkBackground,
    background    = DarkBackground,
    onBackground  = DarkOnSurface,
    surface       = DarkSurface,
    onSurface     = DarkOnSurface,
    // you can tweak tertiary if you want a third accent color
    tertiary      = NeonGreenDark,
    onTertiary    = DarkBackground,
)

private val LightColorScheme = lightColorScheme(
    primary       = NeonGreen,
    onPrimary     = LightOnSurface,
    secondary     = AquaDark,
    onSecondary   = LightOnSurface,
    background    = LightBackground,
    onBackground  = LightOnSurface,
    surface       = LightSurface,
    onSurface     = LightOnSurface,
    tertiary      = NeonGreenDark,
    onTertiary    = LightOnSurface,
)

@Composable
fun MaxMediaPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Android 12+ dynamic
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx)
            else dynamicLightColorScheme(ctx)
        }
        // static dark
        darkTheme -> DarkColorScheme
        // static light
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme  = colorScheme,
        typography   = Typography,   // keep your existing Typography.kt
       // shapes       = Shapes,       // keep your existing Shapes.kt
        content      = content
    )
}
