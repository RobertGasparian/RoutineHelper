package com.robertgasparian.routinehelper.ui.dsm

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Preview(name = "Phone - Light", showBackground = true, widthDp = 393, heightDp = 852)
@Preview(
    name = "Phone - Dark",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun NoteEditorDialogPhonePreview() {
    NoteEditorDialogPreviewContent()
}

@Preview(name = "Landscape - Light", showBackground = true, widthDp = 852, heightDp = 393)
@Preview(
    name = "Landscape - Dark",
    showBackground = true,
    widthDp = 852,
    heightDp = 393,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun NoteEditorDialogLandscapePreview() {
    NoteEditorDialogPreviewContent()
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun NoteEditorDialogTabletPreview() {
    NoteEditorDialogPreviewContent()
}

@Preview(name = "Foldable", showBackground = true, widthDp = 673, heightDp = 841)
@Composable
private fun NoteEditorDialogFoldablePreview() {
    NoteEditorDialogPreviewContent()
}

@Composable
private fun NoteEditorDialogPreviewContent() {
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
