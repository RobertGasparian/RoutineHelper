package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
internal fun rememberRoutineReorderAutoScroller(
    listState: LazyListState,
    edgeThreshold: Dp = ReorderAutoScrollEdgeThreshold,
    maxScrollPerFrame: Dp = ReorderAutoScrollMaxStep,
): RoutineReorderAutoScroller {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val edgeThresholdPx = with(density) { edgeThreshold.toPx() }
    val maxScrollPerFramePx = with(density) { maxScrollPerFrame.toPx() }
    val autoScroller = remember(
        listState,
        coroutineScope,
        edgeThresholdPx,
        maxScrollPerFramePx,
    ) {
        RoutineReorderAutoScroller(
            listState = listState,
            coroutineScope = coroutineScope,
            edgeThresholdPx = edgeThresholdPx,
            maxScrollPerFramePx = maxScrollPerFramePx,
        )
    }

    DisposableEffect(autoScroller) {
        onDispose(autoScroller::stop)
    }

    return autoScroller
}

internal class RoutineReorderAutoScroller(
    private val listState: LazyListState,
    private val coroutineScope: CoroutineScope,
    private val edgeThresholdPx: Float,
    private val maxScrollPerFramePx: Float,
) {
    private var scrollJob: Job? = null

    fun start(
        draggedItemTop: () -> Float?,
        draggedItemSize: () -> Int,
        onScroll: (Float) -> Unit,
    ) {
        stop()
        scrollJob = coroutineScope.launch {
            listState.scroll(MutatePriority.PreventUserInput) {
                while (isActive) {
                    withFrameNanos { }
                    val currentDraggedItemTop = draggedItemTop() ?: continue
                    val currentDraggedItemSize = draggedItemSize()
                    if (currentDraggedItemSize == 0) continue

                    val draggedEnd = currentDraggedItemTop + currentDraggedItemSize
                    val layoutInfo = listState.layoutInfo
                    val scrollDelta = calculateReorderAutoScrollDelta(
                        draggedStart = currentDraggedItemTop,
                        draggedEnd = draggedEnd,
                        viewportStart = layoutInfo.viewportStartOffset,
                        viewportEnd = layoutInfo.viewportEndOffset,
                        edgeThreshold = edgeThresholdPx,
                        maxScrollPerFrame = maxScrollPerFramePx,
                    )
                    if (scrollDelta != 0f) {
                        val consumedScroll = scrollBy(scrollDelta)
                        if (consumedScroll != 0f) {
                            onScroll(consumedScroll)
                        }
                    }
                }
            }
        }
    }

    fun stop() {
        scrollJob?.cancel()
        scrollJob = null
    }
}

internal fun calculateReorderAutoScrollDelta(
    draggedStart: Float,
    draggedEnd: Float,
    viewportStart: Int,
    viewportEnd: Int,
    edgeThreshold: Float,
    maxScrollPerFrame: Float,
): Float {
    val topTrigger = viewportStart + edgeThreshold
    val bottomTrigger = viewportEnd - edgeThreshold
    return when {
        draggedStart < topTrigger -> {
            val edgeProgress = ((topTrigger - draggedStart) / edgeThreshold).coerceIn(0f, 1f)
            -maxScrollPerFrame * edgeProgress
        }
        draggedEnd > bottomTrigger -> {
            val edgeProgress = ((draggedEnd - bottomTrigger) / edgeThreshold).coerceIn(0f, 1f)
            maxScrollPerFrame * edgeProgress
        }
        else -> 0f
    }
}

private val ReorderAutoScrollEdgeThreshold = 64.dp
private val ReorderAutoScrollMaxStep = 12.dp
