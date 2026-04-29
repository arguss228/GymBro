package com.gymbro.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════════
//  Dark colour scheme — deep navy, electric blue, vivid orange
// ═══════════════════════════════════════════════════════════════
private val DarkColorScheme = darkColorScheme(
    // Primary — Electric Blue
    primary                = PrimaryBlue,
    onPrimary              = OnPrimaryWhite,
    primaryContainer       = PrimaryContainer,
    onPrimaryContainer     = OnPrimaryContainer,

    // Secondary — Cyan/Teal
    secondary              = SecondaryCyan,
    onSecondary            = Color.Black,
    secondaryContainer     = Color(0xFF004D40),
    onSecondaryContainer   = SecondaryCyanLight,

    // Tertiary — Orange
    tertiary               = TertiaryOrange,
    onTertiary             = Color.Black,
    tertiaryContainer      = Color(0xFF7B3000),
    onTertiaryContainer    = TertiaryOrangeLight,

    // Surfaces
    background             = DarkBackground,
    onBackground           = DarkOnBackground,
    surface                = DarkSurface,
    onSurface              = DarkOnSurface,
    surfaceVariant         = DarkSurfaceVariant,
    onSurfaceVariant       = DarkOnSurfaceVariant,

    // Outline
    outline                = DarkOutline,
    outlineVariant         = DarkOutlineVariant,

    // Error
    error                  = ErrorRed,
    onError                = Color.White,
    errorContainer         = ErrorContainer,
    onErrorContainer       = OnErrorContainer,

    // Inverse
    inverseSurface         = DarkOnSurface,
    inverseOnSurface       = DarkSurface,
    inversePrimary         = PrimaryBlueLight,

    // Scrim / shadow
    scrim                  = Color.Black,
)

// ═══════════════════════════════════════════════════════════════
//  Light colour scheme — clean whites, electric blue, vivid accents
// ═══════════════════════════════════════════════════════════════
private val LightColorScheme = lightColorScheme(
    // Primary — Electric Blue (slightly darker for contrast on white)
    primary                = PrimaryBlueDark,
    onPrimary              = OnPrimaryWhite,
    primaryContainer       = PrimaryContainerL,
    onPrimaryContainer     = OnPrimaryContainerL,

    // Secondary — Teal
    secondary              = SecondaryCyanDark,
    onSecondary            = Color.White,
    secondaryContainer     = Color(0xFFB2DFDB),
    onSecondaryContainer   = Color(0xFF00352D),

    // Tertiary — Orange
    tertiary               = TertiaryOrangeDark,
    onTertiary             = Color.White,
    tertiaryContainer      = Color(0xFFFFDBC8),
    onTertiaryContainer    = Color(0xFF5C1700),

    // Surfaces
    background             = LightBackground,
    onBackground           = LightOnBackground,
    surface                = LightSurface,
    onSurface              = LightOnSurface,
    surfaceVariant         = LightSurfaceVariant,
    onSurfaceVariant       = LightOnSurfaceVariant,

    // Outline
    outline                = LightOutline,
    outlineVariant         = LightOutlineVariant,

    // Error
    error                  = Color(0xFFB00020),
    onError                = Color.White,
    errorContainer         = ErrorContainerL,
    onErrorContainer       = OnErrorContainerL,

    // Inverse
    inverseSurface         = LightOnSurface,
    inverseOnSurface       = LightSurface,
    inversePrimary         = PrimaryBlue,

    scrim                  = Color.Black,
)

// ═══════════════════════════════════════════════════════════════
//  AppTheme — entry point
// ═══════════════════════════════════════════════════════════════

/**
 * Главная тема приложения.
 *
 * @param themeMode  выбранный пользователем режим (Light / Dark / System).
 *                   Передаётся из [MainActivity] через [ThemeViewModel].
 * @param dynamicColor  использовать Material You Dynamic Color (Android 12+).
 *                      По умолчанию включено; при невозможности — fallback на кастомную схему.
 */
@Composable
fun GymBroTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val context = LocalContext.current

    val colorScheme = when {
        // Dynamic Color (Material You) — Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context)
            else        dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else   -> LightColorScheme
    }

    // Status bar & navigation bar styling
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = GymBroTypography,
        content     = content,
    )
}