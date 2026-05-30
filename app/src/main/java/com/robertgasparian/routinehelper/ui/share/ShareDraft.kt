package com.robertgasparian.routinehelper.ui.share

data class ShareDraft(
    val mode: ShareMode,
    val messageText: String,
    val fileText: String? = null,
) {
    val isFileShare: Boolean = mode == ShareMode.File

    companion object {
        fun text(messageText: String): ShareDraft =
            ShareDraft(
                mode = ShareMode.Text,
                messageText = messageText,
            )

        fun file(
            messageText: String,
            fileText: String,
        ): ShareDraft =
            ShareDraft(
                mode = ShareMode.File,
                messageText = messageText,
                fileText = fileText,
            )
    }
}

enum class ShareMode {
    Text,
    File,
}
