package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.domain.model.RoutineCadence

@Composable
fun RoutineNoteBlock(
    note: String,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.46f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!label.isNullOrBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun CadenceChip(
    cadence: RoutineCadence,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = cadence.icon,
                contentDescription = null,
            )
            Text(
                text = cadence.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun CompletionChip(
    isChecked: Boolean,
    isRepeatAction: Boolean,
    completedCount: Int,
    repeatTargetCount: Int?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (isChecked) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = if (isChecked) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Default.Check else Icons.Default.Remove,
                contentDescription = null,
            )
            Text(
                text = if (isRepeatAction) {
                    "${completedCount.coerceAtLeast(0)}/${repeatTargetCount ?: 0}"
                } else if (isChecked) {
                    "Checked"
                } else {
                    "Unchecked"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun RepeatCountControl(
    completedCount: Int,
    repeatTargetCount: Int,
    onCompletedCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            RepeatCountButton(
                enabled = completedCount > 0,
                onClick = { onCompletedCountChange((completedCount - 1).coerceAtLeast(0)) },
                contentDescription = "Decrease completed count",
                icon = Icons.Default.Remove,
            )
            Text(
                text = "${completedCount.coerceIn(0, repeatTargetCount)}/$repeatTargetCount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            RepeatCountButton(
                enabled = completedCount < repeatTargetCount,
                onClick = { onCompletedCountChange((completedCount + 1).coerceAtMost(repeatTargetCount)) },
                contentDescription = "Increase completed count",
                icon = Icons.Default.Add,
            )
        }
    }
}

@Composable
private fun RepeatCountButton(
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
        )
    }
}

val RoutineCadence.label: String
    get() = when (this) {
        RoutineCadence.Daily -> "Daily"
        RoutineCadence.Weekly -> "Weekly"
    }

private val RoutineCadence.icon
    get() = when (this) {
        RoutineCadence.Daily -> Icons.Default.DateRange
        RoutineCadence.Weekly -> Icons.Default.Refresh
    }
