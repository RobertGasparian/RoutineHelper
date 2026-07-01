package com.robertgasparian.routinehelper.ui.share

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.ui.dsm.RoutineKeyboardAwareBottomActions
import com.robertgasparian.routinehelper.ui.dsm.RoutineOutlinedTextField
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

sealed interface ShareTextPreviewIntent {
    data object BackClick : ShareTextPreviewIntent

    data object CancelClick : ShareTextPreviewIntent

    data object ShareClick : ShareTextPreviewIntent

    data class TextChange(
        val text: String,
    ) : ShareTextPreviewIntent
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
        onIntent = { event ->
            when (event) {
                ShareTextPreviewIntent.BackClick,
                ShareTextPreviewIntent.CancelClick -> onBackClick()
                ShareTextPreviewIntent.ShareClick -> onShareClick(text)
                is ShareTextPreviewIntent.TextChange -> text = event.text
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareTextPreviewComponent(
    text: String,
    onIntent: (ShareTextPreviewIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOverSoftLimit = text.length > SHARE_TEXT_SOFT_LIMIT

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onIntent(ShareTextPreviewIntent.BackClick) }) {
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
                onCancelClick = { onIntent(ShareTextPreviewIntent.CancelClick) },
                onShareClick = { onIntent(ShareTextPreviewIntent.ShareClick) },
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
                onValueChange = { value -> onIntent(ShareTextPreviewIntent.TextChange(value)) },
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
    RoutineKeyboardAwareBottomActions(
        primaryText = "Share text",
        primaryEnabled = canShare,
        onPrimaryClick = onShareClick,
        secondaryText = "Cancel",
        onSecondaryClick = onCancelClick,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 852)
@Composable
private fun ShareTextPreviewScreenPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareTextPreviewComponent(
            text = previewShareText,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 852, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareTextPreviewScreenDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareTextPreviewComponent(
            text = previewShareText,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 852)
@Composable
private fun ShareTextPreviewLongWarningPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareTextPreviewComponent(
            text = previewShareText + "\n\n" + "A".repeat(SHARE_TEXT_SOFT_LIMIT),
            onIntent = {},
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
