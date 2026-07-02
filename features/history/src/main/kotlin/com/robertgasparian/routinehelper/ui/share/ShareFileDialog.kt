package com.robertgasparian.routinehelper.ui.share

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogFilledButton
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogTextButton
import com.robertgasparian.routinehelper.ui.dsm.RoutineOutlinedTextField
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import java.io.File

const val SHARE_TEXT_SOFT_LIMIT = 4_000

@Composable
fun ShareFileDialog(
    draft: ShareDraft.File,
    onFileNameChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onShareClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        title = {
            Text(text = "Export .txt")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShareFileDescriptionBlock()
                RoutineOutlinedTextField(
                    value = draft.fileName,
                    onValueChange = onFileNameChange,
                    label = "File name",
                    isRequired = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                RoutineOutlinedTextField(
                    value = draft.messageText,
                    onValueChange = onTextChange,
                    label = "Message",
                    minLines = 3,
                    maxLines = 5,
                    supportingText = {
                        Text(text = "${draft.messageText.length} characters")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            FlowRow(
                horizontalArrangement = Arrangement.End,
            ) {
                RoutineDialogFilledButton(
                    text = "Share .txt",
                    enabled = draft.messageText.isNotBlank() && draft.fileName.isNotBlank(),
                    onClick = onShareClick,
                )
            }
        },
        dismissButton = {
            RoutineDialogTextButton(
                text = "Cancel",
                onClick = onDismiss,
            )
        },
    )
}

@Composable
private fun ShareFileDialogPreviewContent(
    draft: ShareDraft.File,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .widthIn(max = 360.dp)
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Export .txt",
                style = MaterialTheme.typography.headlineSmall,
            )
            ShareFileDescriptionBlock()
            RoutineOutlinedTextField(
                value = draft.fileName,
                onValueChange = {},
                label = "File name",
                isRequired = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            RoutineOutlinedTextField(
                value = draft.messageText,
                onValueChange = {},
                label = "Message",
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text(text = "${draft.messageText.length} characters")
                },
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                RoutineDialogTextButton(
                    text = "Cancel",
                    onClick = {},
                )
                RoutineDialogFilledButton(
                    text = "Share .txt",
                    enabled = draft.messageText.isNotBlank() && draft.fileName.isNotBlank(),
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun ShareFileDescriptionBlock(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = "A text file will be attached. You can edit the file name and message.",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

fun Context.shareText(
    text: String,
    title: String,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(sendIntent, title))
}

fun Context.shareTextFile(
    fileText: String,
    messageText: String,
    title: String,
    fileName: String = "routine-snapshot-export.txt",
) {
    val exportsDir = File(cacheDir, "shared_exports").apply {
        mkdirs()
    }
    val exportFile = File(exportsDir, fileName.normalizedTxtFileName()).apply {
        writeText(fileText)
    }
    val uri = FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        exportFile,
    )
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, messageText)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(sendIntent, title))
}

private fun String.normalizedTxtFileName(): String {
    val cleaned = trim()
        .replace(Regex("""[\\/:*?"<>|]"""), "-")
        .ifBlank { "routine-snapshot-export" }
    return if (cleaned.endsWith(".txt", ignoreCase = true)) {
        cleaned
    } else {
        "$cleaned.txt"
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ShareFileDialogPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareFileDialogPreviewContainer {
            ShareFileDialogPreviewContent(
                draft = ShareDraft.file(
                    messageText = "Here is the daily routine snapshot from 2026-05-29.",
                    fileText = previewFileText,
                    fileName = "routine-snapshot-2026-05-29.txt",
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareFileDialogDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareFileDialogPreviewContainer {
            ShareFileDialogPreviewContent(
                draft = ShareDraft.file(
                    messageText = "Here is the daily routine snapshot from 2026-05-29.",
                    fileText = previewFileText,
                    fileName = "routine-snapshot-2026-05-29.txt",
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ShareFileDialogEmptyFileNamePreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareFileDialogPreviewContainer {
            ShareFileDialogPreviewContent(
                draft = ShareDraft.file(
                    messageText = "Here is the daily routine snapshot from 2026-05-29.",
                    fileText = previewFileText,
                    fileName = "",
                ),
            )
        }
    }
}

@Composable
private fun ShareFileDialogPreviewContainer(
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}

private val previewFileText = """
    Routine snapshot
    Date: 2026-05-29
    Finalized: 11:45 PM

    1. [x] Drink water
       Note: One liter was diet soda.
""".trimIndent()
