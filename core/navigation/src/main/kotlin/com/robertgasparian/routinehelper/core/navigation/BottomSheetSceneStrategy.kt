package com.robertgasparian.routinehelper.core.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import kotlinx.coroutines.flow.first

/**
 * Displays entries marked with [bottomSheet] in a Material 3 modal bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val sheetEntry = entries.lastOrNull() ?: return null
        val configuration = sheetEntry.metadata[BottomSheetConfigurationKey] ?: return null
        val previousEntries = entries.dropLast(1)
        check(previousEntries.isNotEmpty()) {
            "A bottom-sheet entry must have a parent entry below it on the back stack"
        }

        return BottomSheetScene(
            key = sheetEntry.contentKey,
            previousEntries = previousEntries,
            overlaidEntries = previousEntries,
            entry = sheetEntry,
            configuration = configuration,
            onBack = onBack,
        )
    }

    companion object {
        fun bottomSheet(
            skipPartiallyExpanded: Boolean = false,
        ): Map<String, Any> = metadata {
            put(
                BottomSheetConfigurationKey,
                BottomSheetConfiguration(
                    skipPartiallyExpanded = skipPartiallyExpanded,
                ),
            )
        }

        private object BottomSheetConfigurationKey : NavMetadataKey<BottomSheetConfiguration>
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private data class BottomSheetConfiguration(
    val skipPartiallyExpanded: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
private class BottomSheetScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val configuration: BottomSheetConfiguration,
    private val onBack: () -> Unit,
) : OverlayScene<T> {
    override val entries: List<NavEntry<T>> = listOf(entry)

    private lateinit var sheetState: SheetState

    override val content: @Composable () -> Unit = {
        val lifecycleOwner = rememberLifecycleOwner()
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = if (configuration.skipPartiallyExpanded) {
                setOf(SheetValue.Hidden, SheetValue.Expanded)
            } else {
                setOf(
                    SheetValue.Hidden,
                    SheetValue.PartiallyExpanded,
                    SheetValue.Expanded,
                )
            },
        )
        var presentationState by remember(sheetState) {
            mutableStateOf(BottomSheetPresentationState.Presenting)
        }
        LaunchedEffect(sheetState) {
            snapshotFlow {
                sheetState.isVisible && !sheetState.isAnimationRunning
            }.first { isPresented -> isPresented }
            presentationState = BottomSheetPresentationState.Presented
        }

        ModalBottomSheet(
            onDismissRequest = onBack,
            sheetState = sheetState,
        ) {
            CompositionLocalProvider(
                LocalLifecycleOwner provides lifecycleOwner,
                LocalBottomSheetPresentationState provides presentationState,
            ) {
                entry.Content()
            }
        }
    }

    override suspend fun onRemove() {
        if (this::sheetState.isInitialized) {
            sheetState.hide()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BottomSheetScene<*>

        return key == other.key &&
            previousEntries == other.previousEntries &&
            overlaidEntries == other.overlaidEntries &&
            entry == other.entry &&
            configuration == other.configuration
    }

    override fun hashCode(): Int {
        return key.hashCode() * 31 +
            previousEntries.hashCode() * 31 +
            overlaidEntries.hashCode() * 31 +
            entry.hashCode() * 31 +
            configuration.hashCode() * 31
    }
}
