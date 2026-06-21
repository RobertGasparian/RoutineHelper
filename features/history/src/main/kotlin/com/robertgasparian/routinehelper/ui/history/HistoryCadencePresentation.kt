package com.robertgasparian.routinehelper.ui.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.ui.graphics.vector.ImageVector
import com.robertgasparian.routinehelper.domain.model.RoutineCadence

internal val RoutineCadence.historyLabel: String
    get() = when (this) {
        RoutineCadence.Daily -> "Daily"
        RoutineCadence.Weekly -> "Weekly"
    }

internal val RoutineCadence.historyIcon: ImageVector
    get() = when (this) {
        RoutineCadence.Daily -> Icons.Default.Event
        RoutineCadence.Weekly -> Icons.Default.ViewWeek
    }
