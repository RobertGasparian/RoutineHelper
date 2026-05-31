package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import java.util.Date

@Composable
fun NoteEditorDialog(
    title: String,
    textFieldLabel: String,
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (note: String) -> Unit,
) {
    var note by rememberSaveable(initialNote, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initialNote, selection = TextRange(initialNote.length)))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            NoteTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(text = textFieldLabel) },
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(note.text) }) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}

@Composable
fun NoteTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        minLines = 4,
        maxLines = 8,
        trailingIcon = {
            IconButton(
                onClick = {
                    val currentTime = android.text.format.DateFormat
                        .getTimeFormat(context)
                        .format(Date())
                    onValueChange(value.insertAtCursor(currentTime))
                },
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Insert current time",
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun NoteEditorDialogPreview() {
    RoutineHelperTheme {
        NoteEditorDialog(
            title = "Edit note",
            textFieldLabel = "Today note",
            initialNote = "08:30 Walked before breakfast.",
            onDismiss = {},
            onConfirm = {},
        )
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
