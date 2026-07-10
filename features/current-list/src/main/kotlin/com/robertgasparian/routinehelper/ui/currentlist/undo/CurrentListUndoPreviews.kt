package com.robertgasparian.routinehelper.ui.currentlist.undo

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
private fun CurrentListUndoPhonePreview() {
    RoutineHelperTheme {
        CurrentListUndoPreviewContent()
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
private fun CurrentListUndoLandscapePreview() {
    RoutineHelperTheme {
        CurrentListUndoPreviewContent()
    }
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun CurrentListUndoTabletPreview() {
    RoutineHelperTheme {
        CurrentListUndoPreviewContent()
    }
}

@Preview(name = "Foldable", showBackground = true, widthDp = 673, heightDp = 841)
@Composable
private fun CurrentListUndoFoldablePreview() {
    RoutineHelperTheme {
        CurrentListUndoPreviewContent()
    }
}

@Composable
private fun CurrentListUndoPreviewContent() {
    Surface(color = MaterialTheme.colorScheme.background) {
        CurrentListUndoSnackbarHost(
            uiState = CurrentListUndoUiState.preview(),
            onIntent = {},
        )
    }
}
