package com.gymbro.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = EnergyOrange,
    onPrimary = Color.Black,
    primaryContainer = EnergyOrangeDark,
    onPrimaryContainer = Color.White,

    secondary = ElitePurple,
    onSecondary = Color.White,
    secondaryContainer = ElitePurpleLight,
    onSecondaryContainer = Color.Black,

    tertiary = AdvancedGold,
    onTertiary = Color.Black,
    tertiaryContainer = AdvancedGoldDark,
    onTertiaryContainer = Color.Black,

    background = SurfaceDarkBase,
    onBackground = TextPrimaryDark,
    surface = SurfaceDarkElevated,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkCard,
    onSurfaceVariant = TextSecondaryDark,

    error = ErrorRed,
    onError = Color.White,
)

private val LightColors = lightColorScheme(
    primary = EnergyOrangeDark,
    onPrimary = Color.White,
    secondary = ElitePurple,
    onSecondary = Color.White,
    tertiary = AdvancedGoldDark,
    onTertiary = Color.Black,
    background = SurfaceLightBase,
    onBackground = TextPrimaryLight,
    surface = SurfaceLightElevated,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLightCard,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorRed,
    onError = Color.White,
)

@Composable
fun GymBroTheme(
    // По умолчанию тёмная — независимо от системной темы.
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GymBroTypography,
        content = content,
    )
}
