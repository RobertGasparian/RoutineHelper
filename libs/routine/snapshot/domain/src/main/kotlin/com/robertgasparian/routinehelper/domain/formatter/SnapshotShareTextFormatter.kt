package com.robertgasparian.routinehelper.domain.formatter

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class SnapshotShareTextFormatter @Inject constructor(
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(snapshot: RoutineSnapshot): String {
        val timeFormatter = shareTimeFormatter.withZone(timeProvider.now().zone)
        return buildString {
            appendLine("${snapshot.cadence.label} routine snapshot")
            appendLine("${snapshot.cadence.dateLabel}: ${snapshot.periodStartDate}")
            appendLine("Finalized: ${timeFormatter.format(Instant.ofEpochMilli(snapshot.finalizedAtMillis))}")
            appendLine()

            snapshot.summaryNote
                ?.takeIf(String::isNotBlank)
                ?.let { summaryNote ->
                    appendLine("Summary note:")
                    appendLine(summaryNote)
                    appendLine()
                }

            if (snapshot.items.isEmpty()) {
                appendLine(snapshot.cadence.emptySnapshotText)
                return@buildString
            }

            snapshot.items
                .sortedBy(RoutineSnapshotItem::position)
                .forEachIndexed { index, item ->
                    appendLine("${index + 1}. ${item.statusLabel} ${item.title}")
                    if (item.repeatTargetCount != null) {
                        appendLine("   Count: ${item.completedCount}/${item.repeatTargetCount}")
                    }
                    item.description
                        ?.takeIf(String::isNotBlank)
                        ?.let { description -> appendLine("   Description: $description") }
                    item.note
                        ?.takeIf(String::isNotBlank)
                        ?.let { note -> appendLine("   Note: $note") }
                    if (index < snapshot.items.lastIndex) appendLine()
                }
        }.trimEnd()
    }

    operator fun invoke(snapshots: List<RoutineSnapshot>): String =
        snapshots
            .sortedWith(compareByDescending<RoutineSnapshot> { it.periodStartDate }.thenByDescending { it.finalizedAtMillis })
            .joinToString(separator = "\n\n---\n\n") { snapshot -> invoke(snapshot) }

    private val RoutineSnapshotItem.statusLabel: String
        get() = when {
            isHidden -> "[skipped]"
            isChecked -> "[x]"
            else -> "[ ]"
        }
}

private val RoutineCadence.label: String
    get() = when (this) {
        RoutineCadence.Daily -> "Daily"
        RoutineCadence.Weekly -> "Weekly"
    }

private val RoutineCadence.dateLabel: String
    get() = when (this) {
        RoutineCadence.Daily -> "Date"
        RoutineCadence.Weekly -> "Week of"
    }

private val RoutineCadence.emptySnapshotText: String
    get() = when (this) {
        RoutineCadence.Daily -> "No actions were saved for this day."
        RoutineCadence.Weekly -> "No actions were saved for this week."
    }

private val shareTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.US)
