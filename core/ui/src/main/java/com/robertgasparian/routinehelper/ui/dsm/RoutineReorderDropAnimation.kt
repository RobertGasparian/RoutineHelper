package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
internal fun <Item> RoutineReorderDropAnimation(
    reorderState: RoutineReorderState<Item>,
    itemId: (Item) -> Long,
    onOrderChange: (List<Long>) -> Unit,
) {
    val isDropAnimating = reorderState.isDropAnimating
    val density = LocalDensity.current
    val dropSpeedPxPerSecond = with(density) {
        DraggedItemDropSpeedDpPerSecond.dp.toPx()
    }

    LaunchedEffect(reorderState, isDropAnimating, dropSpeedPxPerSecond) {
        if (!isDropAnimating) return@LaunchedEffect

        try {
            withFrameNanos { }
            val initialTop = reorderState.draggedItemTop
            val targetTop = reorderState.draggedItemSlotTop

            if (initialTop != null && targetTop != null && initialTop != targetTop) {
                val durationMillis = calculateRoutineReorderDropDurationMillis(
                    initialTop = initialTop,
                    targetTop = targetTop,
                    speedPxPerSecond = dropSpeedPxPerSecond,
                )
                animate(
                    initialValue = initialTop,
                    targetValue = targetTop,
                    animationSpec = tween(
                        durationMillis = durationMillis,
                        easing = LinearEasing,
                    ),
                ) { value, _ ->
                    reorderState.onDropAnimationFrame(itemTop = value)
                }
            }

            val orderedIds = reorderState.onDragEnd(itemId)
            if (orderedIds != null) {
                onOrderChange(orderedIds)
            }
        } finally {
            if (reorderState.isDropAnimating) {
                reorderState.onDragCancel()
            }
        }
    }
}

internal fun calculateRoutineReorderDropDurationMillis(
    initialTop: Float,
    targetTop: Float,
    speedPxPerSecond: Float,
): Int {
    require(speedPxPerSecond > 0f)
    val distancePx = (targetTop - initialTop).absoluteValue
    if (distancePx == 0f) return 0

    return (distancePx / speedPxPerSecond * MillisecondsPerSecond)
        .roundToInt()
        .coerceAtLeast(1)
}

private const val DraggedItemDropSpeedDpPerSecond = 800f
private const val MillisecondsPerSecond = 1_000
