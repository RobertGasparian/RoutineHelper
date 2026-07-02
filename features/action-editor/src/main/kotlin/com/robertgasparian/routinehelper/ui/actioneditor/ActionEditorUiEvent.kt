package com.robertgasparian.routinehelper.ui.actioneditor

sealed interface ActionEditorUiEvent {
    data object Saved : ActionEditorUiEvent

    data object Deleted : ActionEditorUiEvent
}
