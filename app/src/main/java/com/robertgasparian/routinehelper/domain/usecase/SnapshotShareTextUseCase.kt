package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SnapshotShareTextUseCase @Inject constructor() {
    operator fun invoke(snapshot: RoutineDaySnapshot): String = buildString {
        appendLine("${snapshot.cadence.label} routine snapshot")
        appendLine("${snapshot.cadence.dateLabel}: ${snapshot.date}")
        appendLine("Finalized: ${timeFormatter.format(Instant.ofEpochMilli(snapshot.finalizedAtMillis))}")
        appendLine()

        if (snapshot.items.isEmpty()) {
            appendLine("No actions were saved for this day.")
            return@buildString
        }

        snapshot.items
            .sortedBy(RoutineDaySnapshotItem::position)
            .forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.statusLabel} ${item.title}")
                item.description
                    ?.takeIf(String::isNotBlank)
                    ?.let { description -> appendLine("   Description: $description") }
                item.note
                    ?.takeIf(String::isNotBlank)
                    ?.let { note -> appendLine("   Note: $note") }
                if (index < snapshot.items.lastIndex) appendLine()
            }
    }.trimEnd()

    operator fun invoke(snapshots: List<RoutineDaySnapshot>): String =
        snapshots
            .sortedWith(compareByDescending<RoutineDaySnapshot> { it.date }.thenByDescending { it.finalizedAtMillis })
            .joinToString(separator = "\n\n---\n\n") { snapshot -> invoke(snapshot) }

    private val RoutineDaySnapshotItem.statusLabel: String
        get() = if (isChecked) "[x]" else "[ ]"
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

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
