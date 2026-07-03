package com.robertgasparian.routinehelper.ui.app

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

internal enum class HorizontalDirection {
    Left,
    Right,
}

internal fun horizontalSlideContentTransform(
    direction: HorizontalDirection,
): ContentTransform {
    val sign = if (direction == HorizontalDirection.Left) 1 else -1
    return slideInHorizontally { width -> sign * width } togetherWith
        slideOutHorizontally { width -> -sign * width }
}
