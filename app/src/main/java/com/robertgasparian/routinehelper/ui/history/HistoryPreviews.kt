package com.robertgasparian.routinehelper.ui.history

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
private fun HistoryComponentPhonePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
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
private fun HistoryComponentLandscapePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun HistoryComponentTabletPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}

@Preview(name = "Foldable", showBackground = true, widthDp = 673, heightDp = 841)
@Composable
private fun HistoryComponentFoldablePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentEmptyPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewEmpty(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}

@Preview(name = "Selection", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentSelectionPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewSelection(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}

@Preview(name = "Share Dialog", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentSharePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewShare(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}

@Preview(name = "Share Options", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentShareOptionsPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewShareOptions(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}

@Preview(name = "File Share Dialog", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentFileSharePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewFileShare(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}

@Preview(name = "Long Share Warning", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentLongSharePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewLongShare(),
            onSnapshotClick = {},
            onSnapshotLongClick = {},
            onClearSelectionClick = {},
            onShareSelectedClick = {},
            onShareAsTextClick = {},
            onShareAsFileClick = {},
            onDeleteSelectedClick = {},
            onShareTextChange = {},
            onShareDismiss = {},
            onShareTextConfirm = {},
            onShareFileConfirm = {},
        )
    }
}
