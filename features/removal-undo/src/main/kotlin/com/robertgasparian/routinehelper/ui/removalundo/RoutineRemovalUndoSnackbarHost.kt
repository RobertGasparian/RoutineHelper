package com.robertgasparian.routinehelper.ui.removalundo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource
import com.robertgasparian.routinehelper.features.removalundo.R
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun RoutineRemovalUndoSnackbarHost(
    uiState: RoutineRemovalUndoUiState,
    onIntent: (RoutineRemovalUndoIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when (uiState.activeSource) {
        RoutineRemovalSource.CurrentList -> pluralStringResource(
            R.plurals.removal_undo_current_list_items_removed,
            uiState.pendingItemCount,
            uiState.pendingItemCount,
        )
        RoutineRemovalSource.Daily -> pluralStringResource(
            R.plurals.removal_undo_daily_actions_removed,
            uiState.pendingItemCount,
            uiState.pendingItemCount,
        )
        RoutineRemovalSource.Weekly -> pluralStringResource(
            R.plurals.removal_undo_weekly_actions_removed,
            uiState.pendingItemCount,
            uiState.pendingItemCount,
        )
        null -> ""
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = RoutineRemovalUndoSnackbarBottomPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = uiState.isVisible,
            enter = slideInVertically(initialOffsetY = { height -> height }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { height -> height }) + fadeOut(),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 12.dp,
                        end = 8.dp,
                        bottom = 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.inversePrimary,
                            ),
                            onClick = { onIntent(RoutineRemovalUndoIntent.UndoLatestClick) },
                        ) {
                            Text(text = stringResource(R.string.removal_undo_latest))
                        }
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.inversePrimary,
                            ),
                            onClick = { onIntent(RoutineRemovalUndoIntent.UndoAllClick) },
                        ) {
                            Text(text = stringResource(R.string.removal_undo_all))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun RoutineRemovalUndoSnackbarHostPreview() {
    RoutineHelperTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RoutineRemovalUndoSnackbarHost(
                uiState = RoutineRemovalUndoUiState.preview(),
                onIntent = {},
            )
        }
    }
}

private val RoutineRemovalUndoSnackbarBottomPadding = 112.dp
