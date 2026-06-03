package com.robertgasparian.routinehelper.ui.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TopLevelBackStack<T : Any>(
    startKey: T,
) {
    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    fun addTopLevel(key: T) {
        topLevelKey = key
        backStack.clear()
        backStack.add(key)
    }

    fun add(key: T) {
        backStack.add(key)
    }

    fun removeLast(): Boolean {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
            return true
        }

        return false
    }
}
