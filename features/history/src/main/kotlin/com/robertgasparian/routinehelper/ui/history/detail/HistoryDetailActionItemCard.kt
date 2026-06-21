package com.robertgasparian.routinehelper.ui.history.detail

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import com.robertgasparian.routinehelper.ui.theme.routineCompletedColor
import com.robertgasparian.routinehelper.ui.theme.routineCompletedContainerColor
import com.robertgasparian.routinehelper.ui.theme.routineOnCompletedContainerColor

@Composable
fun HistoryDetailActionItemCard(
    item: HistoryDetailItemUiState,
    modifier: Modifier = Modifier,
) {
    val isComplete = item.isComplete
    val isHidden = item.isHidden

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isHidden) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isHidden) {
                    SnapshotHiddenMark()
                } else {
                    SnapshotCompletionMark(isComplete = isComplete)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (isComplete && !isHidden) TextDecoration.LineThrough else null,
                        color = if (isHidden) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    if (!item.description.isNullOrBlank()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (item.isRepeatAction && !isHidden) {
                        SnapshotRepeatProgress(
                            completedCount = item.completedCount,
                            repeatTargetCount = item.repeatTargetCount.orZero(),
                            isComplete = isComplete,
                        )
                    }
                }
            }

            if (!item.note.isNullOrBlank()) {
                SnapshotActionNote(text = item.note)
            }
        }
    }
}

@Composable
private fun SnapshotHiddenMark(
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = shape,
            )
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            imageVector = Icons.Default.Block,
            contentDescription = "Hidden action",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SnapshotCompletionMark(
    isComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    val backgroundColor = if (isComplete) {
        routineCompletedContainerColor()
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (isComplete) {
        routineOnCompletedContainerColor()
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = if (isComplete) {
        routineCompletedColor()
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = backgroundColor,
                shape = shape,
            )
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            imageVector = if (isComplete) Icons.Default.Check else Icons.Default.Remove,
            contentDescription = null,
            tint = if (isComplete) contentColor else contentColor.copy(alpha = 0.56f),
        )
    }
}

@Composable
private fun SnapshotRepeatProgress(
    completedCount: Int,
    repeatTargetCount: Int,
    isComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    val safeTarget = repeatTargetCount.coerceAtLeast(0)
    val safeCompleted = completedCount.coerceIn(0, safeTarget)
    val progress = if (safeTarget == 0) {
        0f
    } else {
        safeCompleted.toFloat() / safeTarget.toFloat()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "$safeCompleted of $safeTarget completed",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isComplete) {
                routineCompletedColor()
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = if (isComplete) {
                routineCompletedColor()
            } else {
                MaterialTheme.colorScheme.primary
            },
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }
}

@Composable
private fun SnapshotActionNote(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

private val HistoryDetailItemUiState.isComplete: Boolean
    get() = !isHidden && if (isRepeatAction) {
        completedCount >= repeatTargetCount.orZero()
    } else {
        isChecked
    }

private fun Int?.orZero(): Int = this ?: 0

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun HistoryDetailActionItemCardPreview() {
    RoutineHelperTheme {
        HistoryDetailActionItemCardPreviewContent()
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HistoryDetailActionItemCardDarkPreview() {
    RoutineHelperTheme {
        HistoryDetailActionItemCardPreviewContent()
    }
}

@Composable
private fun HistoryDetailActionItemCardPreviewContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 390.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HistoryDetailActionItemCard(
                item = HistoryDetailItemUiState(
                    actionId = 1,
                    title = "Morning stretch",
                    description = "Ten minutes of mobility.",
                    repeatTargetCount = null,
                    completedCount = 0,
                    isChecked = true,
                    note = "Completed before breakfast.",
                ),
            )
            HistoryDetailActionItemCard(
                item = HistoryDetailItemUiState(
                    actionId = 2,
                    title = "Read book",
                    description = "At least 20 pages.",
                    repeatTargetCount = null,
                    completedCount = 0,
                    isChecked = false,
                    note = "Stopped halfway through chapter four.",
                ),
            )
            HistoryDetailActionItemCard(
                item = HistoryDetailItemUiState(
                    actionId = 3,
                    title = "Drink water",
                    description = "Three bottles before dinner.",
                    repeatTargetCount = 3,
                    completedCount = 3,
                    isChecked = false,
                    note = "Finished the last bottle after dinner.",
                ),
            )
            HistoryDetailActionItemCard(
                item = HistoryDetailItemUiState(
                    actionId = 33,
                    title = "Breathing reset",
                    description = "Short pauses during work.",
                    repeatTargetCount = 14,
                    completedCount = 14,
                    isChecked = false,
                    note = "Worked best before meetings.",
                ),
            )
            HistoryDetailActionItemCard(
                item = HistoryDetailItemUiState(
                    actionId = 4,
                    title = "Walk outside",
                    description = null,
                    repeatTargetCount = 2,
                    completedCount = 1,
                    isChecked = false,
                    isHidden = true,
                    note = null,
                ),
            )
            HistoryDetailActionItemCard(
                item = HistoryDetailItemUiState(
                    actionId = 5,
                    title = "Journal",
                    description = null,
                    repeatTargetCount = null,
                    completedCount = 0,
                    isChecked = false,
                    note = null,
                ),
            )
        }
    }
}
