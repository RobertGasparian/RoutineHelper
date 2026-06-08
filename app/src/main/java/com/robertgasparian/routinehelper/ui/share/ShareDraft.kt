package com.robertgasparian.routinehelper.ui.share

data class ShareDraft(
    val mode: ShareMode,
    val messageText: String,
    val fileText: String? = null,
    val fileName: String? = null,
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
            fileName: String,
        ): ShareDraft =
            ShareDraft(
                mode = ShareMode.File,
                messageText = messageText,
                fileText = fileText,
                fileName = fileName,
            )
    }
}

enum class ShareMode {
    Text,
    File,
}
