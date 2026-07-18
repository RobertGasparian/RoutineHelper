package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import kotlinx.coroutines.launch

/**
 * A full end-to-start swipe alternative to [RoutineSwipeToReveal].
 *
 * Reaching Material's dismissal threshold, including through fling velocity, invokes [onDismiss].
 * Prefer [RoutineSwipeToReveal] when a destructive action must require a separate explicit tap.
 * The state is reset before [onDismiss] so the caller remains responsible for removing the item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineSwipeToDismiss(
    onDismiss: () -> Unit,
    backgroundContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    positionalThresholdFraction: Float = RoutineSwipeToDismissDefaults.PositionalThresholdFraction,
    content: @Composable () -> Unit,
) {
    require(positionalThresholdFraction in 0f..1f) {
        "positionalThresholdFraction must be between 0f and 1f"
    }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * positionalThresholdFraction },
    )
    val coroutineScope = rememberCoroutineScope()
    val currentOnDismiss = rememberUpdatedState(onDismiss)

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = enabled,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                coroutineScope.launch {
                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    currentOnDismiss.value()
                }
            }
        },
        backgroundContent = {
            backgroundContent()
        },
        content = {
            content()
        },
    )
}

/** Default values for [RoutineSwipeToDismiss]. */
object RoutineSwipeToDismissDefaults {
    const val PositionalThresholdFraction: Float = 0.5f
}

@Preview(showBackground = true)
@Composable
private fun RoutineSwipeToDismissPreview() {
    RoutineSwipeToDismissPreviewContent()
}

@Composable
internal fun RoutineSwipeToDismissPreviewContent() {
    RoutineHelperTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                RoutineSwipeToDismiss(
                    onDismiss = {},
                    backgroundContent = {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(text = "Pack lunch")
                        }
                    }
                }
            }
        }
    }
}
