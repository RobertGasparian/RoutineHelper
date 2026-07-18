package com.robertgasparian.routinehelper.ui.removalundo

sealed interface RoutineRemovalUndoIntent {
    data object UndoLatestClick : RoutineRemovalUndoIntent

    data object UndoAllClick : RoutineRemovalUndoIntent
}
