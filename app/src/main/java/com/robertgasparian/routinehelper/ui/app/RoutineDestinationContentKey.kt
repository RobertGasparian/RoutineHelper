package com.robertgasparian.routinehelper.ui.app

/**
 * Stable, saveable content keys for entries that can own navigation-flow state.
 *
 * One-time presentation actions are deliberately excluded so restoring or deep-linking to the
 * same logical entry does not create a different flow owner.
 */
internal const val DailyNavigationContentKey = "routine:daily"
internal const val WeeklyNavigationContentKey = "routine:weekly"

internal fun historyDetailNavigationContentKey(snapshotId: Long): String =
    "history:detail:$snapshotId"
