package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * A horizontally anchored container that reveals one trailing action without invoking it from the
 * swipe itself.
 *
 * The caller owns [isRevealed], which makes it possible to keep only one row open at a time. A
 * [backgroundContent] underlay can cover the full item bounds while [actionContent] remains limited
 * to the revealed action width. A click supplied through [actionContent] invokes [onAction], which
 * is expected to remove the containing item. The containing Lazy layout owns the removal animation;
 * this component does not add a second action-driven exit phase.
 */
@Composable
fun RoutineSwipeToReveal(
    isRevealed: Boolean,
    onRevealedChange: (Boolean) -> Unit,
    onAction: () -> Unit,
    actionContent: @Composable (onClick: () -> Unit) -> Unit,
    backgroundContent: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    revealWidth: Dp = RoutineSwipeToRevealDefaults.RevealWidth,
    content: @Composable () -> Unit,
) {
    require(revealWidth > 0.dp) { "revealWidth must be greater than 0.dp" }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val revealOffset = with(density) {
        calculateRoutineSwipeRevealOffset(
            revealWidthPx = revealWidth.toPx(),
            layoutDirection = layoutDirection,
        )
    }
    val anchors = remember(revealOffset) {
        DraggableAnchors {
            RoutineSwipeToRevealValue.Covered at 0f
            RoutineSwipeToRevealValue.Revealed at revealOffset
        }
    }
    val state = remember(anchors) {
        AnchoredDraggableState(
            initialValue = if (isRevealed && enabled) {
                RoutineSwipeToRevealValue.Revealed
            } else {
                RoutineSwipeToRevealValue.Covered
            },
            anchors = anchors,
        )
    }
    val motionScheme = MaterialTheme.motionScheme
    val motionSpec = remember(motionScheme) {
        motionScheme.fastSpatialSpec<Float>()
    }
    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = state,
        positionalThreshold = { distance -> distance * RevealThresholdFraction },
        animationSpec = motionSpec,
    )
    val currentOnRevealedChange = rememberUpdatedState(onRevealedChange)
    val currentOnAction = rememberUpdatedState(onAction)
    var isActionRunning by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        var lastReportedValue = state.settledValue
        snapshotFlow { state.settledValue }
            .distinctUntilChanged()
            .collect { settledValue ->
                if (settledValue != lastReportedValue) {
                    lastReportedValue = settledValue
                    currentOnRevealedChange.value(
                        settledValue == RoutineSwipeToRevealValue.Revealed,
                    )
                }
            }
    }

    LaunchedEffect(isRevealed, enabled, state, motionSpec, isActionRunning) {
        if (!isActionRunning) {
            state.animateTo(
                targetValue = if (isRevealed && enabled) {
                    RoutineSwipeToRevealValue.Revealed
                } else {
                    RoutineSwipeToRevealValue.Covered
                },
                animationSpec = motionSpec,
            )
        }
    }

    val performAction: () -> Unit = {
        if (!isActionRunning) {
            isActionRunning = true
            currentOnRevealedChange.value(false)
            currentOnAction.value()
        }
    }
    val isActionAvailable = isRevealed && enabled && !isActionRunning

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    val foregroundOffset = state.requireOffset()
                    alpha = if (foregroundOffset.absoluteValue > UnderlayVisibilityThresholdPx) {
                        1f
                    } else {
                        0f
                    }
                },
        ) {
            Box(modifier = Modifier.matchParentSize()) {
                backgroundContent()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(revealWidth)
                    .then(
                        if (isActionAvailable) {
                            Modifier
                        } else {
                            Modifier.clearAndSetSemantics { }
                        },
                    ),
            ) {
                actionContent(performAction)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = state.requireOffset().roundToInt(),
                        y = 0,
                    )
                }
                .anchoredDraggable(
                    state = state,
                    reverseDirection = false,
                    orientation = Orientation.Horizontal,
                    enabled = enabled && !isActionRunning,
                    flingBehavior = flingBehavior,
                ),
        ) {
            content()
        }
    }
}

/** Default values for [RoutineSwipeToReveal]. */
object RoutineSwipeToRevealDefaults {
    val RevealWidth: Dp = 80.dp
}

private enum class RoutineSwipeToRevealValue {
    Covered,
    Revealed,
}

internal fun calculateRoutineSwipeRevealOffset(
    revealWidthPx: Float,
    layoutDirection: LayoutDirection,
): Float {
    require(revealWidthPx > 0f)
    return when (layoutDirection) {
        LayoutDirection.Ltr -> -revealWidthPx
        LayoutDirection.Rtl -> revealWidthPx
    }
}

private const val RevealThresholdFraction = 0.5f
private const val UnderlayVisibilityThresholdPx = 0.5f
