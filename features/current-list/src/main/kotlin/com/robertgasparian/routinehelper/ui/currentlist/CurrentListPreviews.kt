package com.robertgasparian.routinehelper.ui.currentlist

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
private fun CurrentListComponentPhonePreview() {
    RoutineHelperTheme {
        CurrentListPreviewContent(uiState = CurrentListUiState.preview())
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
private fun CurrentListComponentLandscapePreview() {
    RoutineHelperTheme {
        CurrentListPreviewContent(uiState = CurrentListUiState.preview())
    }
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun CurrentListComponentTabletPreview() {
    RoutineHelperTheme {
        CurrentListPreviewContent(uiState = CurrentListUiState.preview())
    }
}

@Preview(name = "Foldable", showBackground = true, widthDp = 673, heightDp = 841)
@Composable
private fun CurrentListComponentFoldablePreview() {
    RoutineHelperTheme {
        CurrentListPreviewContent(uiState = CurrentListUiState.preview())
    }
}

@Preview(name = "Empty", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CurrentListComponentEmptyPreview() {
    RoutineHelperTheme {
        CurrentListPreviewContent(uiState = CurrentListUiState.previewEmpty())
    }
}

@Composable
private fun CurrentListPreviewContent(uiState: CurrentListUiState) {
    CurrentListComponent(
        uiState = uiState,
        onIntent = {},
    )
}
