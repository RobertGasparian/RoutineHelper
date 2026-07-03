package com.robertgasparian.routinehelper.ui.history

import com.robertgasparian.routinehelper.domain.model.RoutineCadence

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

internal val HistoryFilter.label: String
    get() = snapshotCadence?.historyLabel ?: "All"
