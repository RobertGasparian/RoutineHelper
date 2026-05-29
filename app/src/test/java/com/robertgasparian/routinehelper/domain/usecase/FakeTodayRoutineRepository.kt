package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTodayRoutineRepository : TodayRoutineRepository {
    private val itemsByDate = mutableMapOf<String, MutableStateFlow<List<TodayRoutineItem>>>()
    val checkedChanges = mutableListOf<CheckedChange>()
    val noteChanges = mutableListOf<NoteChange>()
    val resetDates = mutableListOf<String>()

    fun setItems(
        date: String,
        items: List<TodayRoutineItem>,
    ) {
        itemsByDate.getOrPut(date) { MutableStateFlow(emptyList()) }.value = items
    }

    override fun todayItems(date: String): Flow<List<TodayRoutineItem>> =
        itemsByDate.getOrPut(date) { MutableStateFlow(emptyList()) }

    override suspend fun setChecked(
        date: String,
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        checkedChanges += CheckedChange(
            date = date,
            routineItemId = routineItemId,
            isChecked = isChecked,
        )
    }

    override suspend fun updateNote(
        date: String,
        routineItemId: Long,
        note: String?,
    ) {
        noteChanges += NoteChange(
            date = date,
            routineItemId = routineItemId,
            note = note,
        )
    }

    override suspend fun resetDate(date: String) {
        resetDates += date
    }
}

data class CheckedChange(
    val date: String,
    val routineItemId: Long,
    val isChecked: Boolean,
)

data class NoteChange(
    val date: String,
    val routineItemId: Long,
    val note: String?,
)
