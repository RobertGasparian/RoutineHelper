package com.robertgasparian.routinehelper.ui.dsm

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun RoutineNoteDialog(
    note: String,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = if (note.isBlank()) "Add note" else "Edit note",
    supportingText: String = "This note is saved for this day only.",
    placeholder: String = "Note",
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        RoutineNoteDialogContent(
            note = note,
            onNoteChange = onNoteChange,
            onDismiss = onDismiss,
            onSaveClick = onSaveClick,
            title = title,
            supportingText = supportingText,
            placeholder = placeholder,
            modifier = modifier,
        )
    }
}

@Composable
fun RoutineNoteDialogContent(
    note: String,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = if (note.isBlank()) "Add note" else "Edit note",
    supportingText: String = "This note is saved for this day only.",
    placeholder: String = "Note",
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var noteFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(note, selection = TextRange(note.length)))
    }

    LaunchedEffect(note) {
        if (note != noteFieldValue.text) {
            noteFieldValue = TextFieldValue(note, selection = TextRange(note.length))
        }
    }

    fun updateNote(value: TextFieldValue) {
        noteFieldValue = value
        onNoteChange(value.text)
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
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
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
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

                IconButton(
                    onClick = {
                        val currentTime = android.text.format.DateFormat
                            .getTimeFormat(context)
                            .format(Date())
                        updateNote(noteFieldValue.insertAtCursor(currentTime))
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Add timestamp",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            RoutineNoteField(
                value = noteFieldValue,
                onValueChange = ::updateNote,
                placeholder = placeholder,
                focusRequester = focusRequester,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (note.isNotBlank()) {
                    RoutineDialogTextButton(
                        text = "Clear",
                        onClick = {
                            updateNote(TextFieldValue("", selection = TextRange(0)))
                        },
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                RoutineDialogTextButton(
                    text = "Cancel",
                    onClick = onDismiss,
                )
                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(text = "Save")
                }
            }
        }
    }
}

@Composable
private fun RoutineNoteField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp)
                .padding(12.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 104.dp)
                    .focusRequester(focusRequester),
                textStyle = TextStyle.Default.merge(
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                decorationBox = { innerTextField ->
                    if (value.text.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun RoutineNoteDialogAddPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        RoutineNoteDialogPreviewContainer {
            var note by rememberSaveable { mutableStateOf("") }
            RoutineNoteDialogContent(
                note = note,
                onNoteChange = { note = it },
                onDismiss = {},
                onSaveClick = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun RoutineNoteDialogEditPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        RoutineNoteDialogPreviewContainer {
            var note by rememberSaveable { mutableStateOf("Bring the smaller water bottle next time.") }
            RoutineNoteDialogContent(
                note = note,
                onNoteChange = { note = it },
                onDismiss = {},
                onSaveClick = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoutineNoteDialogDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        RoutineNoteDialogPreviewContainer {
            var note by rememberSaveable {
                mutableStateOf("Felt better after moving this to the start of the routine.")
            }
            RoutineNoteDialogContent(
                note = note,
                onNoteChange = { note = it },
                onDismiss = {},
                onSaveClick = {},
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
