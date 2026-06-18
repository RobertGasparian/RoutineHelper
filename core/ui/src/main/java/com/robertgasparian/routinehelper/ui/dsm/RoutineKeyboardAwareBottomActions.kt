package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun RoutineKeyboardAwareBottomActions(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    secondaryText: String,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
) {
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    var previousImeBottom by remember { mutableIntStateOf(0) }
    var isKeyboardActionMode by remember { mutableStateOf(false) }

    LaunchedEffect(imeBottom) {
        isKeyboardActionMode = when {
            imeBottom == 0 -> false
            imeBottom > previousImeBottom -> true
            imeBottom < previousImeBottom -> false
            else -> isKeyboardActionMode
        }
        previousImeBottom = imeBottom
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.ime
                    .union(WindowInsets.navigationBars)
                    .only(WindowInsetsSides.Bottom),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        RoutineBottomActionMorph(
            isKeyboardVisible = isKeyboardActionMode,
            primaryText = primaryText,
            primaryEnabled = primaryEnabled,
            onPrimaryClick = onPrimaryClick,
            secondaryText = secondaryText,
            onSecondaryClick = onSecondaryClick,
        )
    }
}

@Composable
private fun RoutineBottomActionMorph(
    isKeyboardVisible: Boolean,
    primaryText: String,
    primaryEnabled: Boolean,
    onPrimaryClick: () -> Unit,
    secondaryText: String,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(
        targetState = isKeyboardVisible,
        label = "RoutineBottomActionMorph",
    )
    val animationSpec = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val buttonHeight by transition.animateDp(
        transitionSpec = { animationSpec },
        label = "RoutineActionButtonHeight",
    ) { keyboardVisible ->
        if (keyboardVisible) 48.dp else 52.dp
    }
    val containerHeight by transition.animateDp(
        transitionSpec = { animationSpec },
        label = "RoutineActionContainerHeight",
    ) { keyboardVisible ->
        if (keyboardVisible) 48.dp else 114.dp
    }
    val primaryYOffset = 0.dp
    val secondaryYOffset by transition.animateDp(
        transitionSpec = { animationSpec },
        label = "RoutineSecondaryYOffset",
    ) { keyboardVisible ->
        if (keyboardVisible) 0.dp else 62.dp
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight),
    ) {
        val density = LocalDensity.current
        val horizontalGap = 10.dp
        val compactWidth = (maxWidth - horizontalGap) / 2
        val buttonWidth by transition.animateDp(
            transitionSpec = { animationSpec },
            label = "RoutineActionButtonWidth",
        ) { keyboardVisible ->
            if (keyboardVisible) compactWidth else maxWidth
        }
        val primaryXOffset by transition.animateDp(
            transitionSpec = { animationSpec },
            label = "RoutinePrimaryXOffset",
        ) { keyboardVisible ->
            if (keyboardVisible) compactWidth + horizontalGap else 0.dp
        }
        val secondaryXOffset = 0.dp

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = with(density) { primaryXOffset.roundToPx() },
                        y = with(density) { primaryYOffset.roundToPx() },
                    )
                }
                .width(buttonWidth)
                .height(buttonHeight),
        ) {
            Button(
                enabled = primaryEnabled,
                onClick = onPrimaryClick,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(text = primaryText)
            }
        }
        OutlinedButton(
            onClick = onSecondaryClick,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = with(density) { secondaryXOffset.roundToPx() },
                        y = with(density) { secondaryYOffset.roundToPx() },
                    )
                }
                .width(buttonWidth)
                .height(buttonHeight),
        ) {
            Text(text = secondaryText)
        }
    }
}
