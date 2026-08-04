package com.robertgasparian.routinehelper.ui.reflection.api

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.features.reflection.api.R
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun ReflectionCard(
    summaryNote: String,
    label: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true,
    rating: ReflectionRating? = null,
    tags: List<String> = emptyList(),
) {
    val hasNote = summaryNote.isNotBlank()
    val hasReflection = hasNote || rating != null || tags.isNotEmpty()

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant),
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
                ReflectionIcon()

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
                    ReflectionEditButton(
                        hasReflection = hasReflection,
                        onClick = onEditClick,
                    )
                }
            }

            rating?.let { ReflectionRatingIndicator(rating = it) }

            if (tags.isNotEmpty()) {
                ReflectionTagIndicators(tags = tags)
            }

            if (hasNote) {
                ReflectionTextBlock(text = summaryNote)
            }
        }
    }
}

@Composable
private fun ReflectionTagIndicators(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { tag ->
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ReflectionIcon(
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
private fun ReflectionEditButton(
    hasReflection: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = if (hasReflection) Icons.Default.Edit else Icons.Default.Add,
            contentDescription = stringResource(
                if (hasReflection) {
                    R.string.reflection_card_edit_reflection
                } else {
                    R.string.reflection_card_add_reflection
                },
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReflectionRatingIndicator(
    rating: ReflectionRating,
    modifier: Modifier = Modifier,
) {
    val ratingDescription = stringResource(
        R.string.reflection_card_rating,
        rating.value,
        ReflectionRating.MAXIMUM,
    )
    Row(
        modifier = modifier.semantics {
            contentDescription = ratingDescription
        },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReflectionRating.options.forEach { option ->
            Icon(
                imageVector = if (option <= rating.value) {
                    Icons.Filled.Star
                } else {
                    Icons.Outlined.StarOutline
                },
                contentDescription = null,
                tint = if (option <= rating.value) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ReflectionTextBlock(
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
private fun ReflectionCardAllStatesPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ReflectionCardPreviewContent()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 620, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReflectionCardAllStatesDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ReflectionCardPreviewContent()
    }
}

@Composable
private fun ReflectionCardPreviewContent() {
    Column(
        modifier = Modifier
            .widthIn(max = 390.dp)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReflectionCard(
            summaryNote = "",
            label = "Day note",
            onEditClick = {},
        )
        ReflectionCard(
            summaryNote = "Felt easier after moving the walk to the beginning of the routine.",
            label = "Day note",
            onEditClick = {},
            rating = ReflectionRating(4),
        )
        ReflectionCard(
            summaryNote = "",
            label = "Week note",
            onEditClick = {},
            rating = ReflectionRating(3),
        )
        ReflectionCard(
            summaryNote = "Keep the morning list shorter this week. The evening stretch is still useful.",
            label = "Week note",
            onEditClick = {},
        )
        ReflectionCard(
            summaryNote = "Reflection is visible but locked by policy.",
            label = "Day note",
            onEditClick = {},
            isEditable = false,
        )
        ReflectionCard(
            summaryNote = "",
            label = "Week note",
            onEditClick = {},
            isEditable = false,
        )
    }
}
