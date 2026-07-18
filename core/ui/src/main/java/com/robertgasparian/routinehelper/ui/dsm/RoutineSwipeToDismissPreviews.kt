package com.robertgasparian.routinehelper.ui.dsm

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Mobile light", showBackground = true)
@Preview(
    name = "Mobile dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "Landscape light", widthDp = 800, heightDp = 360, showBackground = true)
@Preview(
    name = "Landscape dark",
    widthDp = 800,
    heightDp = 360,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Preview(name = "Foldable", widthDp = 673, heightDp = 841, showBackground = true)
@Composable
private fun RoutineSwipeToDismissResponsivePreviews() {
    RoutineSwipeToDismissPreviewContent()
}
