package com.robertgasparian.routinehelper.ui.share

sealed interface ShareDraft {
    val messageText: String

    data class Text(
        override val messageText: String,
    ) : ShareDraft

    data class File(
        override val messageText: String,
        val fileText: String,
        val fileName: String,
    ) : ShareDraft

    companion object {
        fun text(messageText: String): Text =
            Text(
                messageText = messageText,
            )

        fun file(
            messageText: String,
            fileText: String,
            fileName: String,
        ): File =
            File(
                messageText = messageText,
                fileText = fileText,
                fileName = fileName,
            )
    }
}
