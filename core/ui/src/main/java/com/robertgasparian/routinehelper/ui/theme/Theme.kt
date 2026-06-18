package com.robertgasparian.routinehelper.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = RoutineSlate,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCEE5FF),
    onPrimaryContainer = Color(0xFF001D32),
    secondary = RoutineSage,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3E8D4),
    onSecondaryContainer = Color(0xFF0E1F13),
    tertiary = RoutineGraphite,
    tertiaryContainer = Color(0xFFDFE3E6),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFF8F9FA),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF3F4F5),
    surfaceContainer = Color(0xFFEDEEEF),
    surfaceContainerHigh = Color(0xFFE7E8E9),
    surfaceContainerHighest = Color(0xFFE1E3E4),
    onSurface = Color(0xFF191C1D),
    onSurfaceVariant = Color(0xFF43474D),
    outline = Color(0xFF73777D),
    outlineVariant = Color(0xFFC3C7CD),
)

private val DarkColorScheme = darkColorScheme(
    primary = RoutineSlateDark,
    onPrimary = Color(0xFF003353),
    primaryContainer = Color(0xFF304960),
    onPrimaryContainer = Color(0xFFCEE5FF),
    secondary = RoutineSageDark,
    onSecondary = Color(0xFF243427),
    secondaryContainer = Color(0xFF394B3C),
    onSecondaryContainer = Color(0xFFD3E8D4),
    tertiary = RoutineGraphiteDark,
    tertiaryContainer = Color(0xFF42474A),
    background = Color(0xFF101415),
    surface = Color(0xFF101415),
    surfaceContainerLowest = Color(0xFF0B0F10),
    surfaceContainerLow = Color(0xFF191C1D),
    surfaceContainer = Color(0xFF1D2021),
    surfaceContainerHigh = Color(0xFF272A2B),
    surfaceContainerHighest = Color(0xFF323536),
    onSurface = Color(0xFFE1E3E4),
    onSurfaceVariant = Color(0xFFC3C7CD),
    outline = Color(0xFF8D9197),
    outlineVariant = Color(0xFF43474D),
)

@Composable
fun RoutineHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
