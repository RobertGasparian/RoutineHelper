package com.robertgasparian.routinehelper.ui.share

data class ShareTextPreviewUiState(
    val text: String = "",
) {
    val canShare: Boolean = text.isNotBlank()
    val isOverSoftLimit: Boolean = text.length > SHARE_TEXT_SOFT_LIMIT

    companion object {
        fun preview(): ShareTextPreviewUiState =
            ShareTextPreviewUiState(text = previewShareText)

        fun previewLongWarning(): ShareTextPreviewUiState =
            ShareTextPreviewUiState(
                text = previewShareText + "\n\n" + "A".repeat(SHARE_TEXT_SOFT_LIMIT),
            )
    }
}

private const val SHARE_TEXT_SOFT_LIMIT = 4_000

private val previewShareText = """
    Daily routine snapshot - 2026-05-29

    Completed 3 of 4 actions.

    - Stretching: done
    - Read Book: not done
      Note: Chapter 4 was very interesting.
""".trimIndent()
