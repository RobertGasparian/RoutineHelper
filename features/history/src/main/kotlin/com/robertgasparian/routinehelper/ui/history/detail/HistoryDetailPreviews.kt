package com.robertgasparian.routinehelper.ui.history.detail

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
private fun HistoryDetailComponentPhonePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.preview(),
            onIntent = {},
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
private fun HistoryDetailComponentLandscapePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.preview(),
            onIntent = {},
        )
    }
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun HistoryDetailComponentTabletPreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.preview(),
            onIntent = {},
        )
    }
}

@Preview(name = "Foldable", showBackground = true, widthDp = 673, heightDp = 841)
@Composable
private fun HistoryDetailComponentFoldablePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.preview(),
            onIntent = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentEmptyPreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewEmpty(),
            onIntent = {},
        )
    }
}

@Preview(name = "Add Summary Note", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentWithoutSummaryNotePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewWithoutSummaryNote(),
            onIntent = {},
        )
    }
}

@Preview(name = "Edit Summary Note", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentSummaryNoteEditorPreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewSummaryNoteEditor(),
            onIntent = {},
        )
    }
}

@Preview(name = "Read-only Summary Note", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentReadOnlySummaryNotePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewReadOnlySummaryNote(),
            onIntent = {},
        )
    }
}

@Preview(name = "Share Dialog", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentSharePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewShare(),
            onIntent = {},
        )
    }
}

@Preview(name = "Share Options", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentShareOptionsPreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewShareOptions(),
            onIntent = {},
        )
    }
}

@Preview(name = "File Share Dialog", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentFileSharePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewFileShare(),
            onIntent = {},
        )
    }
}
