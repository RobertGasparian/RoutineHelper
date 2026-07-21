package com.robertgasparian.routinehelper.ui.settings

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
private fun SettingsComponentPhonePreview() {
    SettingsPreviewContent(SettingsUiState.previewNotificationsEnabled())
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
private fun SettingsComponentLandscapePreview() {
    SettingsPreviewContent(SettingsUiState.previewNotificationsEnabled())
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun SettingsComponentTabletPreview() {
    SettingsPreviewContent(SettingsUiState.preview())
}

@Preview(name = "Foldable", showBackground = true, widthDp = 673, heightDp = 841)
@Composable
private fun SettingsComponentFoldablePreview() {
    SettingsPreviewContent(SettingsUiState.preview())
}

@Composable
private fun SettingsPreviewContent(uiState: SettingsUiState) {
    RoutineHelperTheme {
        SettingsComponent(
            uiState = uiState,
            onIntent = {},
        )
    }
}
