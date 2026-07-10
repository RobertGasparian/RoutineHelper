package com.robertgasparian.routinehelper.ui.currentlist.undo

sealed interface CurrentListUndoIntent {
    data object UndoLatestClick : CurrentListUndoIntent

    data object UndoAllClick : CurrentListUndoIntent
}
