package com.robertgasparian.routinehelper.ui.actioneditor

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.features.actioneditor.R
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogTextButton
import com.robertgasparian.routinehelper.ui.dsm.RoutineKeyboardAwareBottomActions
import com.robertgasparian.routinehelper.ui.dsm.RoutineOutlinedTextField
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionEditorComponent(
    uiState: ActionEditorUiState,
    onIntent: (ActionEditorIntent) -> Unit,
    modifier: Modifier = Modifier,
    cadence: RoutineCadence = RoutineCadence.Daily,
) {
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onIntent(ActionEditorIntent.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_editor_back),
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(
                            if (uiState.isEditing) {
                                R.string.action_editor_edit_action
                            } else {
                                R.string.action_editor_add_action
                            },
                        ),
                    )
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.action_editor_delete_action),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            ActionEditorBottomActions(
                canSave = uiState.canSave,
                onCancelClick = { onIntent(ActionEditorIntent.BackClick) },
                onSaveClick = { onIntent(ActionEditorIntent.SaveClick) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                ActionEditorFormCard(
                    uiState = uiState,
                    onIntent = onIntent,
                    cadence = cadence,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Box(modifier = Modifier.heightIn(min = 12.dp))
            },
        )
    }

    if (showDeleteConfirmation) {
        DeleteActionConfirmationDialog(
            onDismiss = { showDeleteConfirmation = false },
            onDeleteClick = {
                showDeleteConfirmation = false
                onIntent(ActionEditorIntent.DeleteClick)
            },
        )
    }
}

@Composable
private fun ActionEditorFormCard(
    uiState: ActionEditorUiState,
    onIntent: (ActionEditorIntent) -> Unit,
    cadence: RoutineCadence,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionEditorCadenceRow(cadence = cadence)
            RoutineOutlinedTextField(
                value = uiState.title,
                onValueChange = { title -> onIntent(ActionEditorIntent.TitleChange(title)) },
                label = stringResource(R.string.action_editor_title_label),
                isRequired = true,
                placeholder = stringResource(R.string.action_editor_title_placeholder),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            RoutineOutlinedTextField(
                value = uiState.description,
                onValueChange = { description -> onIntent(ActionEditorIntent.DescriptionChange(description)) },
                label = stringResource(R.string.action_editor_description_label),
                placeholder = stringResource(R.string.action_editor_description_placeholder),
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            RepeatTargetSection(
                uiState = uiState,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun ActionEditorCadenceRow(
    cadence: RoutineCadence,
    modifier: Modifier = Modifier,
) {
    val isWeekly = cadence == RoutineCadence.Weekly

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = if (isWeekly) Icons.Default.ViewWeek else Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(
                    if (isWeekly) R.string.action_editor_weekly_action else R.string.action_editor_daily_action,
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun RepeatTargetSection(
    uiState: ActionEditorUiState,
    onIntent: (ActionEditorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_editor_completion_type),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                RepeatModeButtonGroup(
                    isRepeatEnabled = uiState.isRepeatEnabled,
                    onRepeatEnabledChange = { enabled -> onIntent(ActionEditorIntent.RepeatEnabledChange(enabled)) },
                    modifier = Modifier.weight(1.6f),
                )
            }

            if (uiState.isRepeatEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_editor_target_count),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    RepeatTargetStepper(
                        value = uiState.repeatTargetCount,
                        onValueChange = { targetCount ->
                            onIntent(ActionEditorIntent.RepeatTargetCountChange(targetCount))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RepeatModeButtonGroup(
    isRepeatEnabled: Boolean,
    onRepeatEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(R.string.action_editor_single, R.string.action_editor_repeat)
    val selectedIndex = if (isRepeatEnabled) 1 else 0

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, labelRes ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onRepeatEnabledChange(index == 1) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                    baseShape = RoundedCornerShape(12.dp),
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    activeBorderColor = MaterialTheme.colorScheme.primary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline,
                ),
                icon = {},
            )
            {
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun RepeatTargetStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.width(136.dp),
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            RepeatTargetStepButton(
                enabled = value > 2,
                onClick = { onValueChange((value - 1).coerceAtLeast(2)) },
                icon = Icons.Default.Remove,
                contentDescription = stringResource(R.string.action_editor_decrease_repeat_target),
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            RepeatTargetStepButton(
                onClick = { onValueChange(value + 1) },
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.action_editor_increase_repeat_target),
            )
        }
    }
}

@Composable
private fun RepeatTargetStepButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilledTonalIconButton(
        modifier = modifier.size(36.dp),
        enabled = enabled,
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
private fun ActionEditorBottomActions(
    canSave: Boolean,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RoutineKeyboardAwareBottomActions(
        primaryText = stringResource(R.string.action_editor_save),
        primaryEnabled = canSave,
        onPrimaryClick = onSaveClick,
        secondaryText = stringResource(R.string.action_editor_cancel),
        onSecondaryClick = onCancelClick,
        modifier = modifier,
    )
}

@Composable
private fun DeleteActionConfirmationDialog(
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = stringResource(R.string.action_editor_delete_confirmation_title)) },
        text = {
            Text(text = stringResource(R.string.action_editor_delete_confirmation_message))
        },
        confirmButton = {
            RoutineDialogTextButton(
                text = stringResource(R.string.action_editor_delete),
                onClick = onDeleteClick,
                isDestructive = true,
            )
        },
        dismissButton = {
            RoutineDialogTextButton(
                text = stringResource(R.string.action_editor_cancel),
                onClick = onDismiss,
            )
        },
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun ActionEditorAddEmptyPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ActionEditorComponent(
            uiState = ActionEditorUiState.previewEmpty(),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun ActionEditorAddRepeatPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ActionEditorComponent(
            uiState = ActionEditorUiState.preview().copy(isEditing = false),
            cadence = RoutineCadence.Daily,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun ActionEditorEditWeeklyPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ActionEditorComponent(
            uiState = ActionEditorUiState.preview().copy(isEditing = true),
            cadence = RoutineCadence.Weekly,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActionEditorDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ActionEditorComponent(
            uiState = ActionEditorUiState.preview(),
            cadence = RoutineCadence.Weekly,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ActionEditorDeleteDialogPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        DeleteActionConfirmationDialog(
            onDismiss = {},
            onDeleteClick = {},
        )
    }
}
