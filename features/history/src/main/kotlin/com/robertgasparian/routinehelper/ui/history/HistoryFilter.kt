package com.robertgasparian.routinehelper.ui.history

import androidx.annotation.StringRes
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.features.history.R

enum class HistoryFilter {
    All,
    Daily,
    Weekly,
}

internal val HistoryFilter.snapshotCadence: RoutineCadence?
    get() = when (this) {
        HistoryFilter.All -> null
        HistoryFilter.Daily -> RoutineCadence.Daily
        HistoryFilter.Weekly -> RoutineCadence.Weekly
    }

@get:StringRes
internal val HistoryFilter.labelRes: Int
    get() = when (this) {
        HistoryFilter.All -> R.string.history_filter_all
        HistoryFilter.Daily -> R.string.history_cadence_daily
        HistoryFilter.Weekly -> R.string.history_cadence_weekly
    }
