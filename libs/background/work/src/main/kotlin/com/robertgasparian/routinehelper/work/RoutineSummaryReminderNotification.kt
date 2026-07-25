package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.domain.model.RoutineCadence

sealed interface RoutineSummaryReminderNotification {
    data class WriteSummary(
        val snapshotId: Long,
    ) : RoutineSummaryReminderNotification

    data class BuildRoutine(
        val cadence: RoutineCadence,
    ) : RoutineSummaryReminderNotification
}

/**
 * Android-shell boundary for displaying summary reminder notifications.
 *
 * Background work owns the delivery decision, while the app owns notification resources,
 * PendingIntents, and deep-link destinations.
 */
fun interface RoutineSummaryReminderNotifier {
    fun show(notification: RoutineSummaryReminderNotification)
}
