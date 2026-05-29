package com.robertgasparian.routinehelper.ui.today

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme

@Preview(
    name = "Phone - Light",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Preview(
    name = "Phone - Dark",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TodayComponentPhonePreview() {
    RoutineHelperTheme {
        TodayComponentPreviewContent(uiState = TodayUiState.preview())
    }
}

@Preview(
    name = "Landscape - Light",
    showBackground = true,
    widthDp = 852,
    heightDp = 393,
)
@Preview(
    name = "Landscape - Dark",
    showBackground = true,
    widthDp = 852,
    heightDp = 393,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TodayComponentLandscapePreview() {
    RoutineHelperTheme {
        TodayComponentPreviewContent(uiState = TodayUiState.preview())
    }
}

@Preview(
    name = "Tablet",
    showBackground = true,
    widthDp = 800,
    heightDp = 1280,
)
@Composable
private fun TodayComponentTabletPreview() {
    RoutineHelperTheme {
        TodayComponentPreviewContent(uiState = TodayUiState.preview())
    }
}

@Preview(
    name = "Foldable",
    showBackground = true,
    widthDp = 673,
    heightDp = 841,
)
@Composable
private fun TodayComponentFoldablePreview() {
    RoutineHelperTheme {
        TodayComponentPreviewContent(uiState = TodayUiState.preview())
    }
}

@Preview(
    name = "Empty",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun TodayComponentEmptyPreview() {
    RoutineHelperTheme {
        TodayComponentPreviewContent(uiState = TodayUiState.previewEmpty())
    }
}

@Composable
private fun TodayComponentPreviewContent(
    uiState: TodayUiState,
) {
    TodayComponent(
        uiState = uiState,
        onAddAction = { _, _ -> },
        onCheckedChange = { _, _ -> },
        onNoteChange = { _, _ -> },
    )
}
