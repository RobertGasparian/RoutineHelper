package com.robertgasparian.routinehelper.ui.share

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

const val SHARE_TEXT_SOFT_LIMIT = 4_000

@Composable
fun ShareTextDialog(
    draft: ShareDraft,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onShareClick: () -> Unit,
) {
    val isLongTextShare = !draft.isFileShare && draft.messageText.length > SHARE_TEXT_SOFT_LIMIT

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (draft.isFileShare) "Share .txt" else "Share text")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft.messageText,
                    onValueChange = onTextChange,
                    label = { Text(text = "Message") },
                    minLines = 8,
                    maxLines = 14,
                    supportingText = {
                        Text(text = "${draft.messageText.length} characters")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (isLongTextShare) {
                    Text(
                        text = "This export is over 4,000 characters. Some apps may truncate message text, so sharing as a .txt file is safer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            FlowRow(
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    enabled = draft.messageText.isNotBlank(),
                    onClick = onShareClick,
                ) {
                    Text(text = if (draft.isFileShare) "Share .txt" else "Share text")
                }
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
fun ShareFormatDialog(
    onDismiss: () -> Unit,
    onTextClick: () -> Unit,
    onFileClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Share snapshot") },
        text = {
            Text(text = "Choose how you want to prepare the share message.")
        },
        confirmButton = {
            TextButton(onClick = onFileClick) {
                Text(text = "Share .txt")
            }
        },
        dismissButton = {
            FlowRow(
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = "Cancel")
                }
                TextButton(onClick = onTextClick) {
                    Text(text = "Share text")
                }
            }
        },
    )
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
    val exportFile = File(exportsDir, fileName).apply {
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
