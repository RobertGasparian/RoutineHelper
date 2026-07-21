package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot

internal class FakeHistoryTextProvider : HistoryTextProvider {
    override fun finalizedTime(finalizedAtMillis: Long): String = "12:00 PM"

    override fun snapshotShareText(snapshot: RoutineSnapshot): String =
        if (snapshot.cadence == RoutineCadence.Weekly) {
            "Weekly routine snapshot"
        } else {
            "Daily routine snapshot"
        }

    override fun snapshotsShareText(snapshots: List<RoutineSnapshot>): String =
        snapshots.joinToString(separator = "\n\n---\n\n", transform = ::snapshotShareText)

    override fun snapshotsFileMessage(snapshots: List<RoutineSnapshot>): String {
        val dates = snapshots.map(RoutineSnapshot::periodStartDate).distinct().sorted()
        return when (dates.size) {
            0 -> "Here are the routine snapshots."
            1 -> "Here are the routine snapshots from ${dates.first()}."
            else -> "Here are the routine snapshots from ${dates.first()} to ${dates.last()}."
        }
    }

    override fun snapshotFileMessage(snapshot: RoutineSnapshot): String =
        if (snapshot.cadence == RoutineCadence.Weekly) {
            "Here is the weekly routine snapshot from the week of ${snapshot.periodStartDate}."
        } else {
            "Here is the daily routine snapshot from ${snapshot.periodStartDate}."
        }

    override fun snapshotsFileName(): String = "routine-snapshots-export.txt"

    override fun snapshotFileName(snapshot: RoutineSnapshot): String =
        "routine-snapshot-${snapshot.periodStartDate}.txt"
}
