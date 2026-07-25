# Summary reminders

Summary reminders are owned by `:libs:background:work` and delivered by the Android shell:

1. `RoutineSummaryReminderWorkScheduler` registers unique daily and weekly periodic work.
2. Daily work is initially eligible at 10:00 AM local time every day.
3. Weekly work is initially eligible at 10:00 AM local time each Monday. The daily work remains
   independently eligible on Mondays, so both reminders can be delivered.
4. Each worker delegates to `RoutineSummaryReminderOrchestrator` and the shared
   `WorkerResultRunner`.
5. The orchestrator reads the persisted cadence-specific setting and exits without notifying when
   that reminder is disabled.
6. Before choosing a notification, the orchestrator asks the cadence-specific snapshot
   orchestrator to finalize the completed period. Snapshot finalization is serialized and
   idempotent, so a late snapshot worker cannot overwrite an existing History entry or its summary.

WorkManager provides durable, battery-aware work rather than an exact alarm. Ten o'clock is the
requested eligibility time; Android may execute later because of Doze, power policy, or other
system scheduling decisions.

## Delivery decision

Snapshot finalization deliberately does not store empty snapshots. The reminder orchestrator uses
that invariant for the completed period:

- A matching snapshot means the respective routine contained actions. The notification deep-links
  to that History detail and opens the summary editor, unless that snapshot already has a summary.
- A matching snapshot that already has a summary produces no notification.
- No matching snapshot means the respective routine was empty. The generic notification deep-links
  to the Daily or Weekly top-level tab so the user can start constructing a routine.

The background module emits a typed `RoutineSummaryReminderNotification`. The app implementation of
`RoutineSummaryReminderNotifier` owns localized notification resources, notification IDs,
`PendingIntent` construction, and deep-link URIs.

## Permission and settings

Android 13 and newer require `POST_NOTIFICATIONS`. Enabling either reminder in Settings requests
that runtime permission when necessary; the setting is enabled only after permission is granted.
If the app's notifications are disabled globally, Settings leaves the reminder disabled and opens
Android's app-notification settings. The publisher also checks the global notification state before
posting. Workers and the notification publisher never attempt to open permission UI from the
background.

Both work requests stay registered even when their setting is disabled. The worker checks the
latest persisted setting at execution time, which keeps Settings independent of WorkManager and
ensures process restarts do not require schedule reconstruction from UI state.
