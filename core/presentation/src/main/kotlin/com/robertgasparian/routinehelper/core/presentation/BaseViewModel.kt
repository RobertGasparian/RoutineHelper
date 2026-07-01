package com.robertgasparian.routinehelper.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseViewModel<UiState : Any, Intent : Any, UiEvent : Any> : ViewModel() {
    abstract val uiState: StateFlow<UiState>

    private val mutableUiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<UiEvent> = mutableUiEvents.asSharedFlow()

    final fun onIntent(intent: Intent) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: Intent)

    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(block = block)

    protected fun emitUiEvent(event: UiEvent) {
        mutableUiEvents.tryEmit(event)
    }

    protected fun <T> Flow<T>.stateInViewModel(initialValue: T): StateFlow<T> =
        stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(UI_STATE_STOP_TIMEOUT_MILLIS),
            initialValue = initialValue,
        )

    private companion object {
        const val UI_STATE_STOP_TIMEOUT_MILLIS = 5_000L
    }
}
