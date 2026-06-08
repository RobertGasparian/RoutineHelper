package com.robertgasparian.routinehelper.ui.share

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.ui.dsm.RoutineOutlinedTextField
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

sealed interface ShareTextPreviewUiEvent {
    data object BackClick : ShareTextPreviewUiEvent

    data object CancelClick : ShareTextPreviewUiEvent

    data object ShareClick : ShareTextPreviewUiEvent

    data class TextChange(
        val text: String,
    ) : ShareTextPreviewUiEvent
}

@Composable
fun ShareTextPreviewScreen(
    initialText: String,
    onBackClick: () -> Unit,
    onShareClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable(initialText) { mutableStateOf(initialText) }

    ShareTextPreviewComponent(
        text = text,
        onEvent = { event ->
            when (event) {
                ShareTextPreviewUiEvent.BackClick,
                ShareTextPreviewUiEvent.CancelClick,
                -> onBackClick()
                ShareTextPreviewUiEvent.ShareClick -> onShareClick(text)
                is ShareTextPreviewUiEvent.TextChange -> text = event.text
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareTextPreviewComponent(
    text: String,
    onEvent: (ShareTextPreviewUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOverSoftLimit = text.length > SHARE_TEXT_SOFT_LIMIT

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(ShareTextPreviewUiEvent.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                title = { Text(text = "Share text") },
            )
        },
        bottomBar = {
            ShareTextPreviewBottomActions(
                canShare = text.isNotBlank(),
                onCancelClick = { onEvent(ShareTextPreviewUiEvent.CancelClick) },
                onShareClick = { onEvent(ShareTextPreviewUiEvent.ShareClick) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShareTextPreviewHeader()
            if (isOverSoftLimit) {
                ShareTextLimitNote()
            }
            RoutineOutlinedTextField(
                value = text,
                onValueChange = { value -> onEvent(ShareTextPreviewUiEvent.TextChange(value)) },
                label = "Message",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 12,
                supportingText = {
                    Text(text = "${text.length} characters")
                },
            )
        }
    }
}

@Composable
private fun ShareTextPreviewHeader(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = "Review and edit the message before sharing.",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ShareTextLimitNote(
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.padding(7.dp),
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Long message",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Some apps may truncate long messages. Export as .txt if needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ShareTextPreviewBottomActions(
    canShare: Boolean,
    onCancelClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ShareTextPreviewMorphActions(
            isKeyboardVisible = isKeyboardActionMode,
            canShare = canShare,
            onCancelClick = onCancelClick,
            onShareClick = onShareClick,
        )
    }
}

@Composable
private fun ShareTextPreviewMorphActions(
    isKeyboardVisible: Boolean,
    canShare: Boolean,
    onCancelClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(
        targetState = isKeyboardVisible,
        label = "ShareTextPreviewActionMorph",
    )
    val animationSpec = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val buttonHeight by transition.animateDp(
        transitionSpec = { animationSpec },
        label = "ActionButtonHeight",
    ) { keyboardVisible ->
        if (keyboardVisible) 48.dp else 52.dp
    }
    val containerHeight by transition.animateDp(
        transitionSpec = { animationSpec },
        label = "ActionContainerHeight",
    ) { keyboardVisible ->
        if (keyboardVisible) 48.dp else 114.dp
    }
    val shareYOffset = 0.dp
    val cancelYOffset by transition.animateDp(
        transitionSpec = { animationSpec },
        label = "CancelYOffset",
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
            label = "ActionButtonWidth",
        ) { keyboardVisible ->
            if (keyboardVisible) compactWidth else maxWidth
        }
        val shareXOffset by transition.animateDp(
            transitionSpec = { animationSpec },
            label = "ShareXOffset",
        ) { keyboardVisible ->
            if (keyboardVisible) compactWidth + horizontalGap else 0.dp
        }
        val cancelXOffset = 0.dp

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = with(density) { shareXOffset.roundToPx() },
                        y = with(density) { shareYOffset.roundToPx() },
                    )
                }
                .width(buttonWidth)
                .height(buttonHeight),
        ) {
            Button(
                enabled = canShare,
                onClick = onShareClick,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(text = "Share text")
            }
        }
        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = with(density) { cancelXOffset.roundToPx() },
                        y = with(density) { cancelYOffset.roundToPx() },
                    )
                }
                .width(buttonWidth)
                .height(buttonHeight),
        ) {
            Text(text = "Cancel")
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 852)
@Composable
private fun ShareTextPreviewScreenPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareTextPreviewComponent(
            text = previewShareText,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 852, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareTextPreviewScreenDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareTextPreviewComponent(
            text = previewShareText,
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 852)
@Composable
private fun ShareTextPreviewLongWarningPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareTextPreviewComponent(
            text = previewShareText + "\n\n" + "A".repeat(SHARE_TEXT_SOFT_LIMIT),
            onEvent = {},
        )
    }
}

private val previewShareText = """
    Daily routine snapshot - 2026-05-29

    Completed 3 of 4 actions.

    - Stretching: done
    - Read Book: not done
      Note: Chapter 4 was very interesting.
""".trimIndent()
