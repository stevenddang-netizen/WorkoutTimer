package com.steven.workouttimer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// CompositionLocal to expose glassmorphic mode to child composables
val LocalIsGlassmorphic = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = OnPrimary,
    secondaryContainer = SecondaryDark,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = TimerRed,
    onError = OnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    secondary = Secondary,
    onSecondary = OnPrimary,
    secondaryContainer = SecondaryDark,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = TimerRed,
    onError = OnPrimary
)

private val GlassmorphicColorScheme = darkColorScheme(
    primary = GlassPrimary,
    onPrimary = Color.White,
    primaryContainer = GlassPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = GlassPrimary,
    onSecondary = Color.White,
    secondaryContainer = GlassSurfaceVariant,
    background = Color.Transparent,
    onBackground = GlassOnSurface,
    surface = GlassSurface,
    onSurface = GlassOnSurface,
    surfaceVariant = GlassSurfaceVariant,
    onSurfaceVariant = GlassOnSurfaceVariant,
    error = TimerRed,
    onError = Color.White,
    outline = GlassBorder
)

@Composable
fun StevenWorkoutTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isGlassmorphic: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isGlassmorphic -> GlassmorphicColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (isGlassmorphic) {
                GlassGradientStart.toArgb()
            } else {
                colorScheme.background.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme && !isGlassmorphic
        }
    }

    CompositionLocalProvider(LocalIsGlassmorphic provides isGlassmorphic) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
