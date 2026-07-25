package com.robertgasparian.routinehelper.ui.dsm

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.robertgasparian.routinehelper.core.ui.R
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun RoutineNoteDialog(
    value: TextFieldValue,
    onIntent: (RoutineNoteDialogIntent) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(
        if (value.text.isBlank()) R.string.core_ui_add_note else R.string.core_ui_edit_note,
    ),
    supportingText: String = stringResource(R.string.core_ui_note_saved_for_day),
    label: String = stringResource(R.string.core_ui_note),
    autoFocus: Boolean = true,
    showInsertActions: Boolean = true,
) {
    Dialog(
        onDismissRequest = { onIntent(RoutineNoteDialogIntent.Dismiss) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        RoutineNoteDialogContent(
            value = value,
            onIntent = onIntent,
            title = title,
            supportingText = supportingText,
            label = label,
            autoFocus = autoFocus,
            showInsertActions = showInsertActions,
            modifier = modifier,
        )
    }
}

@Composable
fun RoutineNoteDialogContent(
    value: TextFieldValue,
    onIntent: (RoutineNoteDialogIntent) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(
        if (value.text.isBlank()) R.string.core_ui_add_note else R.string.core_ui_edit_note,
    ),
    supportingText: String = stringResource(R.string.core_ui_note_saved_for_day),
    label: String = stringResource(R.string.core_ui_note),
    autoFocus: Boolean = true,
    showInsertActions: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        modifier = modifier
            .widthIn(max = 342.dp)
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (showInsertActions) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { onIntent(RoutineNoteDialogIntent.WeekdayClick) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = stringResource(R.string.core_ui_add_weekday),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = { onIntent(RoutineNoteDialogIntent.DateClick) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.core_ui_add_today_date),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = { onIntent(RoutineNoteDialogIntent.TimeClick) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = stringResource(R.string.core_ui_add_timestamp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            RoutineNoteField(
                value = value,
                onValueChange = { updatedValue ->
                    onIntent(RoutineNoteDialogIntent.ValueChange(updatedValue))
                },
                label = label,
                focusRequester = focusRequester,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (value.text.isNotBlank()) {
                    RoutineDialogTextButton(
                        text = stringResource(R.string.core_ui_clear),
                        onClick = { onIntent(RoutineNoteDialogIntent.ClearClick) },
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                RoutineDialogTextButton(
                    text = stringResource(R.string.core_ui_cancel),
                    onClick = { onIntent(RoutineNoteDialogIntent.Dismiss) },
                )
                Button(
                    onClick = { onIntent(RoutineNoteDialogIntent.SaveClick) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(text = stringResource(R.string.core_ui_save))
                }
            }
        }
    }
}

@Composable
private fun RoutineNoteField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    RoutineOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        minLines = 6,
        maxLines = 10,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
        ),
    )
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun RoutineNoteDialogAddPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        RoutineNoteDialogPreviewContainer {
            var note by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(TextFieldValue(""))
            }
            RoutineNoteDialogContent(
                value = note,
                onIntent = routineNotePreviewIntentHandler(
                    note = note,
                    onNoteChange = { note = it },
                ),
                autoFocus = false,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun RoutineNoteDialogEditPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        RoutineNoteDialogPreviewContainer {
            var note by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(TextFieldValue("Bring the smaller water bottle next time."))
            }
            RoutineNoteDialogContent(
                value = note,
                onIntent = routineNotePreviewIntentHandler(
                    note = note,
                    onNoteChange = { note = it },
                ),
                autoFocus = false,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoutineNoteDialogDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        RoutineNoteDialogPreviewContainer {
            var note by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(TextFieldValue("Felt better after moving this to the start of the routine."))
            }
            RoutineNoteDialogContent(
                value = note,
                onIntent = routineNotePreviewIntentHandler(
                    note = note,
                    onNoteChange = { note = it },
                ),
                autoFocus = false,
            )
        }
    }
}

@Composable
private fun RoutineNoteDialogPreviewContainer(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private fun routineNotePreviewIntentHandler(
    note: TextFieldValue,
    onNoteChange: (TextFieldValue) -> Unit,
): (RoutineNoteDialogIntent) -> Unit = { intent ->
    when (intent) {
        is RoutineNoteDialogIntent.ValueChange -> onNoteChange(intent.value)
        RoutineNoteDialogIntent.ClearClick -> onNoteChange(TextFieldValue(""))
        RoutineNoteDialogIntent.DateClick -> onNoteChange(note.insertAtCursor("June 8"))
        RoutineNoteDialogIntent.WeekdayClick -> onNoteChange(note.insertAtCursor("Monday"))
        RoutineNoteDialogIntent.TimeClick -> onNoteChange(note.insertAtCursor("8:30 AM"))
        RoutineNoteDialogIntent.Dismiss,
        RoutineNoteDialogIntent.SaveClick -> Unit
    }
}

private fun TextFieldValue.insertAtCursor(textToInsert: String): TextFieldValue {
    val start = minOf(selection.start, selection.end).coerceIn(0, text.length)
    val end = maxOf(selection.start, selection.end).coerceIn(0, text.length)
    val updatedText = text.replaceRange(start, end, textToInsert)
    val updatedCursor = start + textToInsert.length
    return copy(
        text = updatedText,
        selection = TextRange(updatedCursor),
    )
}
