package com.robertgasparian.routinehelper.ui.dsm

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import com.robertgasparian.routinehelper.ui.theme.routineCompletedColor
import com.robertgasparian.routinehelper.ui.theme.routineCompletedContainerColor
import com.robertgasparian.routinehelper.ui.theme.routineOnCompletedContainerColor

@Composable
fun RoutineActionItemCard(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    note: String? = null,
    isChecked: Boolean = false,
    isHidden: Boolean = false,
    repeatTargetCount: Int? = null,
    completedCount: Int = 0,
    onCheckedChange: (Boolean) -> Unit = {},
    onCompletedCountChange: (Int) -> Unit = {},
    onEditActionClick: () -> Unit = {},
    onEditNoteClick: () -> Unit = {},
    onHiddenChange: (Boolean) -> Unit = {},
    dragHandleModifier: Modifier = Modifier,
) {
    val isRepeatAction = repeatTargetCount != null
    val isComplete = !isHidden && if (isRepeatAction) {
        completedCount >= repeatTargetCount.orZero()
    } else {
        isChecked
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
        ),
        colors = CardDefaults.cardColors(
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when {
                    isHidden -> RoutineActionItemCardHiddenMarker()
                    isRepeatAction -> RoutineActionItemCardRepeatCounter(
                        completedCount = completedCount,
                        repeatTargetCount = repeatTargetCount.orZero(),
                        isComplete = isComplete,
                        enabled = true,
                        onCompletedCountChange = onCompletedCountChange,
                    )
                    else -> RoutineActionItemCardCheckControl(
                        checked = isChecked,
                        enabled = true,
                        onClick = { onCheckedChange(!isChecked) },
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (isComplete && !isHidden) TextDecoration.LineThrough else null,
                        color = if (isHidden) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    if (isHidden) {
                        Text(
                            text = "Action is hidden for today",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else if (!description.isNullOrBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                RoutineActionItemCardActionColumn(
                    modifier = Modifier.align(Alignment.Top),
                    dragHandleModifier = dragHandleModifier,
                    noteContentDescription = if (note.isNullOrBlank()) "Add note" else "Edit note",
                    isComplete = isComplete,
                    isHidden = isHidden,
                    onEditActionClick = onEditActionClick,
                    onEditNoteClick = onEditNoteClick,
                    onHiddenChange = onHiddenChange,
                )
            }

            if (!note.isNullOrBlank()) {
                RoutineActionItemCardNote(
                    note = note,
                    isComplete = isComplete,
                )
            }
        }
    }
}

@Composable
private fun RoutineActionItemCardHiddenMarker(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(32.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Default.Block,
                contentDescription = "Hidden action",
            )
        }
    }
}

@Composable
private fun RoutineActionItemCardCheckControl(
    checked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(32.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (checked) {
            routineCompletedContainerColor()
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = if (checked) {
            routineOnCompletedContainerColor()
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (checked) routineCompletedColor() else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checked",
                )
            }
        }
    }
}

@Composable
private fun RoutineActionItemCardRepeatCounter(
    completedCount: Int,
    repeatTargetCount: Int,
    isComplete: Boolean,
    enabled: Boolean,
    onCompletedCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.width(42.dp),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isComplete) {
                routineCompletedContainerColor()
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            contentColor = if (isComplete) {
                routineOnCompletedContainerColor()
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isComplete) {
                routineCompletedColor()
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            RoutineActionItemCardCounterButton(
                enabled = enabled && completedCount < repeatTargetCount,
                onClick = { onCompletedCountChange((completedCount + 1).coerceAtMost(repeatTargetCount)) },
                contentDescription = "Increase completed count",
                icon = if (isComplete) Icons.Default.Check else Icons.Default.Add,
                isComplete = isComplete,
            )
            Text(
                text = "${completedCount.coerceIn(0, repeatTargetCount)}/$repeatTargetCount",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
            RoutineActionItemCardCounterButton(
                enabled = enabled && completedCount > 0,
                onClick = { onCompletedCountChange((completedCount - 1).coerceAtLeast(0)) },
                contentDescription = "Decrease completed count",
                icon = Icons.Default.Remove,
                isComplete = isComplete,
            )
        }
    }
}

@Composable
private fun RoutineActionItemCardCounterButton(
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    FilledTonalIconButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier.size(34.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isComplete) {
                routineCompletedContainerColor()
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (isComplete) {
                routineOnCompletedContainerColor()
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            disabledContainerColor = if (isComplete) {
                routineCompletedContainerColor()
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            disabledContentColor = if (isComplete) {
                routineOnCompletedContainerColor()
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            },
        ),
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun RoutineActionItemCardActionColumn(
    dragHandleModifier: Modifier,
    noteContentDescription: String,
    isComplete: Boolean,
    isHidden: Boolean,
    onEditActionClick: () -> Unit,
    onEditNoteClick: () -> Unit,
    onHiddenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
    ) {
        RoutineActionItemCardSmallIconButton(
            modifier = dragHandleModifier,
            onClick = {},
            contentDescription = "Reorder action",
            icon = Icons.Default.DragIndicator,
            tint = LocalActionTint(isComplete),
            buttonSize = 32.dp,
            iconSize = 20.dp,
        )
        RoutineActionItemCardSmallIconButton(
            onClick = { onHiddenChange(!isHidden) },
            contentDescription = if (isHidden) "Show action" else "Hide action",
            icon = if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            tint = LocalActionTint(isComplete),
            buttonSize = 32.dp,
            iconSize = 20.dp,
        )
        if (!isHidden) {
            RoutineActionItemCardSmallIconButton(
                onClick = onEditActionClick,
                contentDescription = "Edit action",
                icon = Icons.Default.Edit,
                tint = LocalActionTint(isComplete),
                buttonSize = 32.dp,
                iconSize = 20.dp,
            )
        }
        RoutineActionItemCardSmallIconButton(
            onClick = onEditNoteClick,
            contentDescription = noteContentDescription,
            icon = Icons.AutoMirrored.Filled.StickyNote2,
            tint = LocalActionTint(isComplete),
            buttonSize = 32.dp,
            iconSize = 20.dp,
        )
    }
}

@Composable
private fun RoutineActionItemCardSmallIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledTint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
    buttonSize: androidx.compose.ui.unit.Dp = 26.dp,
    iconSize: androidx.compose.ui.unit.Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .size(buttonSize)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else disabledTint,
        )
    }
}

@Composable
private fun LocalActionTint(isComplete: Boolean): Color =
    if (isComplete) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

@Composable
private fun RoutineActionItemCardNote(
    note: String,
    isComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = note,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

private fun Int?.orZero(): Int = this ?: 0

@Preview(showBackground = true, widthDp = 390, heightDp = 1300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoutineActionItemCardAllStatesDarkModePreview() {
    RoutineHelperTheme {
        RoutineActionItemCardAllStatesPreviewContent()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1300)
@Composable
private fun RoutineActionItemCardAllStatesPreview() {
    RoutineHelperTheme {
        RoutineActionItemCardAllStatesPreviewContent()
    }
}

@Composable
private fun RoutineActionItemCardAllStatesPreviewContent() {
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
            RoutineActionItemCard(title = "Drink Water")
            RoutineActionItemCard(
                title = "Morning Meditation",
                description = "10 minutes of mindfulness.",
            )
            RoutineActionItemCard(
                title = "Stretching",
                description = "Full-body routine.",
                isChecked = true,
            )
            RoutineActionItemCard(
                title = "Read Book",
                description = "At least 20 pages.",
                note = "Chapter 4 was very interesting.",
            )
            RoutineActionItemCard(
                title = "Take Vitamins",
                note = "Took with breakfast.",
                isChecked = true,
            )
            RoutineActionItemCard(
                title = "Pushups",
                repeatTargetCount = 5,
                completedCount = 0,
            )
            RoutineActionItemCard(
                title = "Walk Dog",
                description = "Around the park.",
                repeatTargetCount = 5,
                completedCount = 2,
            )
            RoutineActionItemCard(
                title = "Coding Practice",
                description = "Two LeetCode problems.",
                repeatTargetCount = 5,
                completedCount = 5,
            )
            RoutineActionItemCard(
                title = "Journaling",
                description = "Three things I am grateful for.",
                note = "Gratitude for the weather.",
                repeatTargetCount = 5,
                completedCount = 2,
            )
            RoutineActionItemCard(
                title = "Call Family",
                note = "Spoke with Mom.",
                repeatTargetCount = 5,
                completedCount = 5,
            )
        }
    }
}
