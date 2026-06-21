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
            onEvent = {},
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
            onEvent = {},
        )
    }
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun HistoryDetailComponentTabletPreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.preview(),
            onEvent = {},
        )
    }
}

@Preview(name = "Foldable", showBackground = true, widthDp = 673, heightDp = 841)
@Composable
private fun HistoryDetailComponentFoldablePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.preview(),
            onEvent = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentEmptyPreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewEmpty(),
            onEvent = {},
        )
    }
}

@Preview(name = "Share Dialog", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentSharePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewShare(),
            onEvent = {},
        )
    }
}

@Preview(name = "Share Options", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentShareOptionsPreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewShareOptions(),
            onEvent = {},
        )
    }
}

@Preview(name = "File Share Dialog", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryDetailComponentFileSharePreview() {
    RoutineHelperTheme {
        HistoryDetailComponent(
            uiState = HistoryDetailUiState.previewFileShare(),
            onEvent = {},
        )
    }
}
