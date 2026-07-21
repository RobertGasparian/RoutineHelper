package com.robertgasparian.routinehelper.ui.share

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robertgasparian.routinehelper.features.history.R
import com.robertgasparian.routinehelper.ui.dsm.RoutineDialogTextButton
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Composable
fun ShareFormatDialog(
    onDismiss: () -> Unit,
    onTextClick: () -> Unit,
    onFileClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.history_share_snapshot),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.history_share_snapshot_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ShareFormatOptionRow(
                    icon = Icons.AutoMirrored.Filled.Article,
                    title = stringResource(R.string.history_editable_text),
                    description = stringResource(R.string.history_share_text_description),
                    onClick = onTextClick,
                )
                ShareFormatOptionRow(
                    icon = Icons.Default.Description,
                    title = stringResource(R.string.history_txt_file),
                    description = stringResource(R.string.history_export_snapshot_description),
                    onClick = onFileClick,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            RoutineDialogTextButton(
                text = stringResource(R.string.history_cancel),
                onClick = onDismiss,
            )
        },
    )
}

@Composable
private fun ShareFormatDialogPreviewContent(
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
                text = stringResource(R.string.history_share_snapshot),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.history_share_snapshot_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ShareFormatOptionRow(
                icon = Icons.AutoMirrored.Filled.Article,
                title = stringResource(R.string.history_editable_text),
                description = stringResource(R.string.history_share_text_description),
                onClick = {},
            )
            ShareFormatOptionRow(
                icon = Icons.Default.Description,
                title = stringResource(R.string.history_txt_file),
                description = stringResource(R.string.history_export_snapshot_description),
                onClick = {},
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                RoutineDialogTextButton(
                    text = stringResource(R.string.history_cancel),
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun ShareFormatOptionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ShareFormatDialogPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareFormatDialogPreviewContainer {
            ShareFormatDialogPreviewContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareFormatDialogDarkPreview() {
    RoutineHelperTheme(dynamicColor = false) {
        ShareFormatDialogPreviewContainer {
            ShareFormatDialogPreviewContent()
        }
    }
}

@Composable
private fun ShareFormatDialogPreviewContainer(
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}
