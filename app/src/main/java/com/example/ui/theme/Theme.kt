package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NepalCrimsonLight,
    onPrimary = Color(0xFF680010),
    primaryContainer = NepalCrimsonDark,
    onPrimaryContainer = Color(0xFFFFDAD9),
    secondary = NepalNavyLight,
    onSecondary = Color(0xFF003062),
    secondaryContainer = Color(0xFF004787),
    onSecondaryContainer = Color(0xFFD5E4F8),
    tertiary = NepalGoldLight,
    onTertiary = Color(0xFF432C00),
    tertiaryContainer = Color(0xFF604100),
    onTertiaryContainer = Color(0xFFFFDF9E),
    background = DarkBackground,
    onBackground = Color(0xFFE2E2E6),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099)
)

private val LightColorScheme = lightColorScheme(
    primary = NepalCrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD9),
    onPrimaryContainer = Color(0xFF410007),
    secondary = NepalNavySecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5E4F8),
    onSecondaryContainer = Color(0xFF001D36),
    tertiary = NepalGoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDF9E),
    onTertiaryContainer = Color(0xFF261900),
    background = LightBackground,
    onBackground = Color(0xFF191C1E),
    surface = LightSurface,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF74777F)
)

@Composable
fun NepalCalendarTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NepalCalendarTheme(
        themeMode = if (darkTheme) "DARK" else "LIGHT",
        content = content
    )
}
