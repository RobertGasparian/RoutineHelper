package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWeeklyRoutineRepository : WeeklyRoutineRepository {
    private val itemsByWeek = mutableMapOf<String, MutableStateFlow<List<WeeklyRoutineItem>>>()
    private val summaryNotesByWeek = MutableStateFlow<Map<String, String>>(emptyMap())
    val checkedChanges = mutableListOf<WeeklyCheckedChange>()
    val hiddenChanges = mutableListOf<WeeklyHiddenChange>()
    val noteChanges = mutableListOf<WeeklyNoteChange>()
    val countChanges = mutableListOf<WeeklyCountChange>()
    val summaryNoteChanges = mutableListOf<WeeklySummaryNoteChange>()
    val resetWeeks = mutableListOf<String>()

    fun setItems(
        weekStartDate: String,
        items: List<WeeklyRoutineItem>,
    ) {
        itemsByWeek.getOrPut(weekStartDate) { MutableStateFlow(emptyList()) }.value = items
    }

    override fun weeklyItems(weekStartDate: String): Flow<List<WeeklyRoutineItem>> =
        itemsByWeek.getOrPut(weekStartDate) { MutableStateFlow(emptyList()) }

    override fun summaryNote(weekStartDate: String): Flow<String?> =
        summaryNotesByWeek.map { notes -> notes[weekStartDate] }

    fun setSummaryNote(
        weekStartDate: String,
        note: String,
    ) {
        summaryNotesByWeek.value = summaryNotesByWeek.value + (weekStartDate to note)
    }

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

    override suspend fun updateCompletedCount(
        weekStartDate: String,
        routineItemId: Long,
        completedCount: Int,
    ) {
        countChanges += WeeklyCountChange(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            completedCount = completedCount,
        )
    }

    override suspend fun setHidden(
        weekStartDate: String,
        routineItemId: Long,
        isHidden: Boolean,
    ) {
        hiddenChanges += WeeklyHiddenChange(
            weekStartDate = weekStartDate,
            routineItemId = routineItemId,
            isHidden = isHidden,
        )
    }

    override suspend fun updateSummaryNote(
        weekStartDate: String,
        note: String?,
    ) {
        summaryNoteChanges += WeeklySummaryNoteChange(weekStartDate = weekStartDate, note = note)
        summaryNotesByWeek.value = if (note == null) {
            summaryNotesByWeek.value - weekStartDate
        } else {
            summaryNotesByWeek.value + (weekStartDate to note)
        }
    }

    override suspend fun resetWeek(weekStartDate: String) {
        resetWeeks += weekStartDate
        summaryNotesByWeek.value = summaryNotesByWeek.value - weekStartDate
    }
}

data class WeeklyCheckedChange(
    val weekStartDate: String,
    val routineItemId: Long,
    val isChecked: Boolean,
)

data class WeeklyHiddenChange(
    val weekStartDate: String,
    val routineItemId: Long,
    val isHidden: Boolean,
)

data class WeeklyNoteChange(
    val weekStartDate: String,
    val routineItemId: Long,
    val note: String?,
)

data class WeeklyCountChange(
    val weekStartDate: String,
    val routineItemId: Long,
    val completedCount: Int,
)

data class WeeklySummaryNoteChange(
    val weekStartDate: String,
    val note: String?,
)
