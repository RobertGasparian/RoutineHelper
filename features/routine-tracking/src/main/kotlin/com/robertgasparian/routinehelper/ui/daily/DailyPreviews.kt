package com.robertgasparian.routinehelper.ui.daily

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingComponent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiState

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
private fun DailyComponentPhonePreview() {
    RoutineHelperTheme {
        DailyComponentPreviewContent(uiState = RoutineTrackingUiState.preview())
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
private fun DailyComponentLandscapePreview() {
    RoutineHelperTheme {
        DailyComponentPreviewContent(uiState = RoutineTrackingUiState.preview())
    }
}

@Preview(
    name = "Tablet",
    showBackground = true,
    widthDp = 800,
    heightDp = 1280,
)
@Composable
private fun DailyComponentTabletPreview() {
    RoutineHelperTheme {
        DailyComponentPreviewContent(uiState = RoutineTrackingUiState.preview())
    }
}

@Preview(
    name = "Foldable",
    showBackground = true,
    widthDp = 673,
    heightDp = 841,
)
@Composable
private fun DailyComponentFoldablePreview() {
    RoutineHelperTheme {
        DailyComponentPreviewContent(uiState = RoutineTrackingUiState.preview())
    }
}

@Preview(
    name = "Empty",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun DailyComponentEmptyPreview() {
    RoutineHelperTheme {
        DailyComponentPreviewContent(uiState = RoutineTrackingUiState.previewEmpty())
    }
}

@Composable
private fun DailyComponentPreviewContent(
    uiState: RoutineTrackingUiState,
) {
    RoutineTrackingComponent(
        uiState = uiState,
        onIntent = {},
    )
}
