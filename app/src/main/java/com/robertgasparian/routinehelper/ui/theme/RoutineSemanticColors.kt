package com.robertgasparian.routinehelper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun routineCompletedColor(): Color =
    if (isSystemInDarkTheme()) {
        Color(0xFF7DDA8A)
    } else {
        Color(0xFF1E7D35)
    }

@Composable
fun routineCompletedContainerColor(): Color =
    if (isSystemInDarkTheme()) {
        Color(0xFF1F4E2A)
    } else {
        Color(0xFFD7F0D8)
    }

@Composable
fun routineOnCompletedContainerColor(): Color =
    if (isSystemInDarkTheme()) {
        Color(0xFFD7F0D8)
    } else {
        Color(0xFF08210D)
    }
