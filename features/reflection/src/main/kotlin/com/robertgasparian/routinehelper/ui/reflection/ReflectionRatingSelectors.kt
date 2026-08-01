package com.robertgasparian.routinehelper.ui.reflection

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.features.reflection.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CompactPillRatingSelector(
    rating: ReflectionRating?,
    onRatingChange: (ReflectionRating?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSources = remember {
        List(ReflectionRating.options.count()) { MutableInteractionSource() }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.reflection_editor_rating_label),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(
                enabled = rating != null,
                onClick = { onRatingChange(null) },
            ) {
                Text(text = stringResource(R.string.reflection_editor_clear_rating))
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            ButtonGroup(
                overflowIndicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .selectableGroup(),
                expandedRatio = 0.15f,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReflectionRating.options.forEachIndexed { index, option ->
                    val optionRating = ReflectionRating(option)
                    val interactionSource = interactionSources[index]
                    val isSelected = rating == optionRating
                    val isFilled = rating != null && option <= rating.value
                    customItem(
                        buttonGroupContent = {
                            val optionDescription = ratingOptionDescription(option)
                            ToggleButton(
                                checked = isSelected,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) onRatingChange(optionRating)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .animateWidth(
                                        interactionSource = interactionSource,
                                        compressionLimit = PaddingValues(horizontal = 4.dp),
                                    )
                                    .heightIn(min = 48.dp)
                                    .semantics { role = Role.RadioButton },
                                shapes = ToggleButtonDefaults.shapes(
                                    shape = CircleShape,
                                    pressedShape = CircleShape,
                                    checkedShape = CircleShape,
                                ),
                                colors = ToggleButtonDefaults.toggleButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                                contentPadding = PaddingValues(0.dp),
                                interactionSource = interactionSource,
                            ) {
                                RatingStarIcon(
                                    isFilled = isFilled,
                                    tint = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                        isFilled -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    contentDescription = optionDescription,
                                )
                            }
                        },
                        menuContent = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingStarIcon(
    isFilled: Boolean,
    tint: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(24.dp),
    )
}

@Composable
private fun ratingOptionDescription(option: Int): String =
    stringResource(
        R.string.reflection_editor_rating_option,
        option,
        ReflectionRating.MAXIMUM,
    )
