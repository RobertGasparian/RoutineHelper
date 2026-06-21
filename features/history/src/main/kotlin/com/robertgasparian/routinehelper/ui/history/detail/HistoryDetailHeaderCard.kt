package com.robertgasparian.routinehelper.ui.history.detail

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import com.robertgasparian.routinehelper.ui.theme.routineCompletedColor
import com.robertgasparian.routinehelper.ui.theme.routineCompletedContainerColor
import com.robertgasparian.routinehelper.ui.theme.routineOnCompletedContainerColor

@Composable
fun HistoryDetailHeaderCard(
    uiState: HistoryDetailUiState,
    modifier: Modifier = Modifier,
) {
    val completionSummary = uiState.completionSummary

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CadenceChip(cadence = uiState.cadence)
                CompletionSummaryChip(
                    summary = completionSummary,
                )
            }
            Text(
                text = uiState.date.ifBlank { "Snapshot" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
            if (uiState.finalizedLabel.isNotBlank()) {
                Text(
                    text = uiState.finalizedLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CompletionSummaryChip(
    summary: CompletionSummary,
    modifier: Modifier = Modifier,
) {
    val isAllComplete = summary is CompletionSummary.AllComplete

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (isAllComplete) {
            routineCompletedContainerColor()
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = if (isAllComplete) {
            routineOnCompletedContainerColor()
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isAllComplete) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = routineCompletedColor(),
                )
            }
            Text(
                text = summary.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private val HistoryDetailUiState.completionSummary: CompletionSummary
    get() {
        val countableItems = items.filterNot { item -> item.isHidden }
        val totalCount = countableItems.size
        if (totalCount == 0) return CompletionSummary.Empty

        val completedCount = countableItems.count(HistoryDetailItemUiState::isComplete)
        return if (completedCount == totalCount) {
            CompletionSummary.AllComplete
        } else {
            CompletionSummary.Partial(completedCount = completedCount, totalCount = totalCount)
        }
    }

private val HistoryDetailItemUiState.isComplete: Boolean
    get() = repeatTargetCount?.let { target -> completedCount >= target } ?: isChecked

private sealed interface CompletionSummary {
    val label: String

    data object Empty : CompletionSummary {
        override val label: String = "No actions saved"
    }

    data object AllComplete : CompletionSummary {
        override val label: String = "All completed!"
    }

    data class Partial(
        val completedCount: Int,
        val totalCount: Int,
    ) : CompletionSummary {
        override val label: String = "$completedCount of $totalCount completed"
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 520)
@Composable
private fun HistoryDetailHeaderCardPreview() {
    RoutineHelperTheme {
        HistoryDetailHeaderCardPreviewContent()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 520, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryDetailHeaderCardDarkPreview() {
    RoutineHelperTheme {
        HistoryDetailHeaderCardPreviewContent()
    }
}

@Composable
private fun HistoryDetailHeaderCardPreviewContent() {
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
            HistoryDetailHeaderCard(uiState = HistoryDetailUiState.preview())
            HistoryDetailHeaderCard(
                uiState = HistoryDetailUiState.preview().copy(
                    cadence = RoutineCadence.Weekly,
                    date = "Week of 2026-05-25",
                    items = HistoryDetailUiState.preview().items.map { item ->
                        item.copy(isChecked = true, completedCount = item.repeatTargetCount ?: item.completedCount)
                    },
                ),
            )
            HistoryDetailHeaderCard(uiState = HistoryDetailUiState.previewEmpty())
        }
    }
}
