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
private fun HistoryComponentLandscapePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onIntent = {},
        )
    }
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun HistoryComponentTabletPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onIntent = {},
        )
    }
}

@Preview(name = "Foldable", showBackground = true, widthDp = 673, heightDp = 841)
@Composable
private fun HistoryComponentFoldablePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.preview(),
            onIntent = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentEmptyPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewEmpty(),
            onIntent = {},
        )
    }
}

@Preview(name = "Selection", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentSelectionPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewSelection(),
            onIntent = {},
        )
    }
}

@Preview(name = "Share Dialog", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentSharePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewShare(),
            onIntent = {},
        )
    }
}

@Preview(name = "Share Options", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentShareOptionsPreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewShareOptions(),
            onIntent = {},
        )
    }
}

@Preview(name = "File Share Dialog", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentFileSharePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewFileShare(),
            onIntent = {},
        )
    }
}

@Preview(name = "Long Share Warning", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HistoryComponentLongSharePreview() {
    RoutineHelperTheme {
        HistoryComponent(
            uiState = HistoryUiState.previewLongShare(),
            onIntent = {},
        )
    }
}
