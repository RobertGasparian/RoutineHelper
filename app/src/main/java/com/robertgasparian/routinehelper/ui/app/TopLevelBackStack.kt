package com.robertgasparian.routinehelper.ui.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class TopLevelBackStack<T : Any>(
    startKey: T,
) {
    private val topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey),
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    fun addTopLevel(key: T) {
        val existingStack = topLevelStacks.remove(key)
        topLevelStacks[key] = existingStack ?: mutableStateListOf(key)
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast(): Boolean {
        val currentStack = topLevelStacks[topLevelKey] ?: return false
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
            updateBackStack()
            return true
        }

        if (topLevelStacks.size <= 1) return false

        topLevelStacks.remove(topLevelKey)
        topLevelKey = topLevelStacks.keys.last()
        updateBackStack()
        return true
    }

    private fun updateBackStack() {
        backStack.clear()
        backStack.addAll(topLevelStacks.flatMap { it.value })
    }
}
