package com.robertgasparian.routinehelper.ui.dsm

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun SummaryNoteCard(
    note: String,
    label: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true,
) {
    val hasNote = note.isNotBlank()

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                SummaryNoteIcon()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                if (isEditable) {
                    SummaryNoteEditButton(
                        hasNote = hasNote,
                        onClick = onEditClick,
                    )
                }
            }

            if (hasNote) {
                SummaryNoteTextBlock(text = note)
            }
        }
    }
}

@Composable
private fun SummaryNoteIcon(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
            )
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(8.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = Icons.AutoMirrored.Filled.StickyNote2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SummaryNoteEditButton(
    hasNote: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = if (hasNote) Icons.Default.Edit else Icons.Default.Add,
            contentDescription = if (hasNote) "Edit summary note" else "Add summary note",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryNoteTextBlock(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 620)
@Composable
private fun SummaryNoteCardAllStatesPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        SummaryNoteCardPreviewContent()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 620, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SummaryNoteCardAllStatesDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        SummaryNoteCardPreviewContent()
    }
}

@Composable
private fun SummaryNoteCardPreviewContent() {
    Column(
        modifier = Modifier
            .widthIn(max = 390.dp)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryNoteCard(
            note = "",
            label = "Day note",
            onEditClick = {},
        )
        SummaryNoteCard(
            note = "Felt easier after moving the walk to the beginning of the routine.",
            label = "Day note",
            onEditClick = {},
        )
        SummaryNoteCard(
            note = "",
            label = "Week note",
            onEditClick = {},
        )
        SummaryNoteCard(
            note = "Keep the morning list shorter this week. The evening stretch is still useful.",
            label = "Week note",
            onEditClick = {},
        )
        SummaryNoteCard(
            note = "This is a longer note preview to check wrapping. It should stay compact and stop after a few lines instead of turning the summary area into a large writing surface.",
            label = "Day note",
            onEditClick = {},
        )
        SummaryNoteCard(
            note = "Read-only snapshot note with no edit action.",
            label = "Snapshot note",
            onEditClick = {},
            isEditable = false,
        )
    }
}
