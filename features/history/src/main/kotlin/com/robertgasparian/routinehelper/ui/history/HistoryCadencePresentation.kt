package com.robertgasparian.routinehelper.ui.history

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.ui.graphics.vector.ImageVector
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.features.history.R

@get:StringRes
internal val RoutineCadence.historyLabelRes: Int
    get() = when (this) {
        RoutineCadence.Daily -> R.string.history_cadence_daily
        RoutineCadence.Weekly -> R.string.history_cadence_weekly
    }

internal val RoutineCadence.historyIcon: ImageVector
    get() = when (this) {
        RoutineCadence.Daily -> Icons.Default.Event
        RoutineCadence.Weekly -> Icons.Default.ViewWeek
    }
