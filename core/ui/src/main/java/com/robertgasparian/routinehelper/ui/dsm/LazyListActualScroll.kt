package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

@Composable
fun rememberLazyListIsActuallyScrolling(listState: LazyListState): Boolean {
    val tracker = remember(listState) {
        LazyListActualScrollTracker(
            firstVisibleItemIndex = listState.firstVisibleItemIndex,
            firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset,
        )
    }
    var isActuallyScrolling by remember(listState) { mutableStateOf(false) }

    LaunchedEffect(listState, tracker) {
        snapshotFlow {
            LazyListScrollSnapshot(
                isScrollInProgress = listState.isScrollInProgress,
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset,
            )
        }.collect { snapshot ->
            isActuallyScrolling = tracker.update(snapshot)
        }
    }

    return isActuallyScrolling
}

internal class LazyListActualScrollTracker(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
) {
    private var previousPosition = LazyListPosition(
        firstVisibleItemIndex = firstVisibleItemIndex,
        firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
    )
    private var hasScrolledDuringCurrentScroll = false

    fun update(snapshot: LazyListScrollSnapshot): Boolean {
        val currentPosition = LazyListPosition(
            firstVisibleItemIndex = snapshot.firstVisibleItemIndex,
            firstVisibleItemScrollOffset = snapshot.firstVisibleItemScrollOffset,
        )
        val positionChanged = currentPosition != previousPosition
        previousPosition = currentPosition

        hasScrolledDuringCurrentScroll = snapshot.isScrollInProgress &&
            (hasScrolledDuringCurrentScroll || positionChanged)
        return hasScrolledDuringCurrentScroll
    }
}

internal data class LazyListScrollSnapshot(
    val isScrollInProgress: Boolean,
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
)

private data class LazyListPosition(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
)
