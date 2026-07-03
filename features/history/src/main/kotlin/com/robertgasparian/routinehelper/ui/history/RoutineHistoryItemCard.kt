package com.robertgasparian.routinehelper.ui.history

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoutineHistoryItemCard(
    cadence: RoutineCadence,
    title: String,
    completionLabel: String,
    isComplete: Boolean,
    hasSummaryNote: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
) {
    val isWeekly = cadence == RoutineCadence.Weekly

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            null
        } else {
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
            )
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoutineHistoryCadenceIcon(
                cadence = cadence,
                isSelected = isSelected,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = if (isWeekly) "Weekly" else "Daily",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoutineHistoryAccent(isSelected),
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (hasSummaryNote) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                            contentDescription = "Has summary note",
                            tint = RoutineHistoryAccent(isSelected),
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = completionLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isComplete) {
                        RoutineHistoryCompleteColor()
                    } else {
                        RoutineHistorySecondaryText(isSelected)
                    },
                    fontWeight = if (isComplete) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                )
            }

            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = RoutineHistorySecondaryText(isSelected),
                )
            }
        }
    }
}

@Composable
private fun RoutineHistoryCadenceIcon(
    cadence: RoutineCadence,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(32.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = if (isSelected) {
            MaterialTheme.colorScheme.onSecondary
        } else {
            MaterialTheme.colorScheme.primary
        },
        border = if (isSelected) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = cadence.historyIcon,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun RoutineHistoryAccent(isSelected: Boolean) =
    if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

@Composable
private fun RoutineHistorySecondaryText(isSelected: Boolean) =
    if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

@Composable
private fun RoutineHistoryCompleteColor(): Color =
    if (androidx.compose.foundation.isSystemInDarkTheme()) {
        Color(0xFF7DDA8A)
    } else {
        Color(0xFF1E7D35)
    }

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun RoutineHistoryItemCardAllStatesPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        RoutineHistoryItemCardPreviewContent()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoutineHistoryItemCardAllStatesDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        RoutineHistoryItemCardPreviewContent()
    }
}

@Composable
private fun RoutineHistoryItemCardPreviewContent() {
    Column(
        modifier = Modifier
            .widthIn(max = 390.dp)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RoutineHistoryItemCard(
            cadence = RoutineCadence.Daily,
            title = "May 29, 2026",
            completionLabel = "3/5 completed",
            isComplete = false,
            hasSummaryNote = false,
            onClick = {},
            onLongClick = {},
        )
        RoutineHistoryItemCard(
            cadence = RoutineCadence.Daily,
            title = "May 30, 2026",
            completionLabel = "All completed!",
            isComplete = true,
            hasSummaryNote = true,
            onClick = {},
            onLongClick = {},
        )
        RoutineHistoryItemCard(
            cadence = RoutineCadence.Weekly,
            title = "Week of May 25, 2026",
            completionLabel = "8/12 completed",
            isComplete = false,
            hasSummaryNote = false,
            onClick = {},
            onLongClick = {},
        )
        RoutineHistoryItemCard(
            cadence = RoutineCadence.Weekly,
            title = "Week of June 1, 2026",
            completionLabel = "All completed!",
            isComplete = true,
            hasSummaryNote = true,
            onClick = {},
            onLongClick = {},
        )
        RoutineHistoryItemCard(
            cadence = RoutineCadence.Daily,
            title = "May 31, 2026",
            completionLabel = "2/4 completed",
            isComplete = false,
            hasSummaryNote = true,
            isSelectionMode = true,
            isSelected = false,
            onClick = {},
            onLongClick = {},
        )
        RoutineHistoryItemCard(
            cadence = RoutineCadence.Weekly,
            title = "Week of May 18, 2026",
            completionLabel = "6/10 completed",
            isComplete = false,
            hasSummaryNote = true,
            isSelectionMode = true,
            isSelected = true,
            onClick = {},
            onLongClick = {},
        )
    }
}
