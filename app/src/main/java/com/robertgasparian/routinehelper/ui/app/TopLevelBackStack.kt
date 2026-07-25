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

    fun replaceWithTopLevelPath(
        topLevelKey: T,
        nestedKeys: List<T>,
    ) {
        this.topLevelKey = topLevelKey
        backStack.clear()
        backStack.add(topLevelKey)
        backStack.addAll(nestedKeys)
    }

    fun removeLast(): Boolean {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
            return true
        }

        return false
    }

    companion object {
        fun <T : Any> fromRestored(entries: List<T>): TopLevelBackStack<T> {
            require(entries.isNotEmpty()) { "Restored back stack must not be empty." }
            return TopLevelBackStack(entries.first()).apply {
                backStack.clear()
                backStack.addAll(entries)
            }
        }
    }
}
