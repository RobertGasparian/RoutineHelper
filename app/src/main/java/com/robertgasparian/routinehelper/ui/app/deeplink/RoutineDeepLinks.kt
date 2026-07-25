package com.robertgasparian.routinehelper.ui.app.deeplink

import android.net.Uri
import androidx.core.net.toUri
import com.robertgasparian.routinehelper.domain.model.RoutineCadence

object RoutineDeepLinks {
    private const val SCHEME = "routinehelper"
    private const val HISTORY_HOST = "history"
    private const val ROUTINES_HOST = "routines"

    fun historySummaryEdit(snapshotId: Long): Uri {
        require(snapshotId > 0L) { "A history deep link requires a positive snapshot ID." }
        return "$SCHEME://$HISTORY_HOST/snapshots/$snapshotId/summary/edit".toUri()
    }

    fun routine(cadence: RoutineCadence): Uri =
        "$SCHEME://$ROUTINES_HOST/${cadence.toDeepLinkValue()}".toUri()

    internal const val HISTORY_SUMMARY_EDIT_PATTERN =
        "$SCHEME://$HISTORY_HOST/snapshots/{snapshotId}/summary/edit"

    internal const val ROUTINE_PATTERN = "$SCHEME://$ROUTINES_HOST/{cadence}"

    private fun RoutineCadence.toDeepLinkValue(): String =
        when (this) {
            RoutineCadence.Daily -> "daily"
            RoutineCadence.Weekly -> "weekly"
        }
}
