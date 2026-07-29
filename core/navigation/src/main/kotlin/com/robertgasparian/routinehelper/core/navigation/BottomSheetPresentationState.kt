package com.robertgasparian.routinehelper.core.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Initial presentation state for content hosted by [BottomSheetSceneStrategy].
 *
 * This state becomes [Presented] after the sheet's opening animation settles and remains there for
 * the lifetime of that sheet instance. Content outside a bottom-sheet scene is already considered
 * presented.
 */
enum class BottomSheetPresentationState {
    Presenting,
    Presented,
}

val LocalBottomSheetPresentationState = staticCompositionLocalOf {
    BottomSheetPresentationState.Presented
}
