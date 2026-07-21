package com.robertgasparian.routinehelper.ui.weekly

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.robertgasparian.routinehelper.ui.theme.RoutineHelperTheme
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingComponent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiState

@Preview(name = "Phone - Light", showBackground = true, widthDp = 393, heightDp = 852)
@Preview(
    name = "Phone - Dark",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun WeeklyComponentPhonePreview() {
    WeeklyPreviewContent(uiState = RoutineTrackingUiState.preview().copy(date = "2026-05-24"))
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
private fun WeeklyComponentLandscapePreview() {
    WeeklyPreviewContent(uiState = RoutineTrackingUiState.preview().copy(date = "2026-05-24"))
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun WeeklyComponentTabletPreview() {
    WeeklyPreviewContent(uiState = RoutineTrackingUiState.preview().copy(date = "2026-05-24"))
}

@Preview(name = "Foldable", showBackground = true, widthDp = 673, heightDp = 841)
@Composable
private fun WeeklyComponentFoldablePreview() {
    WeeklyPreviewContent(uiState = RoutineTrackingUiState.preview().copy(date = "2026-05-24"))
}

@Preview(name = "Empty", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun WeeklyComponentEmptyPreview() {
    WeeklyPreviewContent(uiState = RoutineTrackingUiState.previewEmpty().copy(date = "2026-05-24"))
}

@Composable
private fun WeeklyPreviewContent(uiState: RoutineTrackingUiState) {
    RoutineHelperTheme {
        RoutineTrackingComponent(
            uiState = uiState,
            onIntent = {},
            cadence = com.robertgasparian.routinehelper.domain.model.RoutineCadence.Weekly,
            showSnapshotAction = false,
        )
    }
}
