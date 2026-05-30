package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWeeklyRoutineRepository : WeeklyRoutineRepository {
    private val itemsByWeek = mutableMapOf<String, MutableStateFlow<List<WeeklyRoutineItem>>>()
    val checkedChanges = mutableListOf<WeeklyCheckedChange>()
    val noteChanges = mutableListOf<WeeklyNoteChange>()
    val resetWeeks = mutableListOf<String>()

    fun setItems(
        weekStartDate: String,
        items: List<WeeklyRoutineItem>,
    ) {
        itemsByWeek.getOrPut(weekStartDate) { MutableStateFlow(emptyList()) }.value = items
    }

    override fun weeklyItems(weekStartDate: String): Flow<List<WeeklyRoutineItem>> =
        itemsByWeek.getOrPut(weekStartDate) { MutableStateFlow(emptyList()) }

    override suspend fun setChecked(
        weekStartDate: String,
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        checkedChanges += WeeklyCheckedChange(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            isChecked = isChecked,
        )
    }

    override suspend fun updateNote(
        weekStartDate: String,
        routineItemId: Long,
        note: String?,
    ) {
        noteChanges += WeeklyNoteChange(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            note = note,
        )
    }

    override suspend fun resetWeek(weekStartDate: String) {
        resetWeeks += weekStartDate
    }
}

data class WeeklyCheckedChange(
    val weekStartDate: String,
    val routineItemId: Long,
    val isChecked: Boolean,
)

data class WeeklyNoteChange(
    val weekStartDate: String,
    val routineItemId: Long,
    val note: String?,
)
