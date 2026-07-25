package com.robertgasparian.routinehelper.ui.app.deeplink

import android.net.Uri
import androidx.core.net.toUri

object RoutineDeepLinks {
    private const val SCHEME = "routinehelper"
    private const val HISTORY_HOST = "history"

    fun historySummaryEdit(snapshotId: Long): Uri {
        require(snapshotId > 0L) { "A history deep link requires a positive snapshot ID." }
        return "$SCHEME://$HISTORY_HOST/snapshots/$snapshotId/summary/edit".toUri()
    }

    internal const val HISTORY_SUMMARY_EDIT_PATTERN =
        "$SCHEME://$HISTORY_HOST/snapshots/{snapshotId}/summary/edit"
}
