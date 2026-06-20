package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTodayRoutineRepository : TodayRoutineRepository {
    private val itemsByDate = mutableMapOf<String, MutableStateFlow<List<TodayRoutineItem>>>()
    private val summaryNotesByDate = MutableStateFlow<Map<String, String>>(emptyMap())
    val checkedChanges = mutableListOf<CheckedChange>()
    val hiddenChanges = mutableListOf<HiddenChange>()
    val noteChanges = mutableListOf<NoteChange>()
    val countChanges = mutableListOf<CountChange>()
    val summaryNoteChanges = mutableListOf<SummaryNoteChange>()
    val resetDates = mutableListOf<String>()

    fun setItems(
        date: String,
        items: List<TodayRoutineItem>,
    ) {
        itemsByDate.getOrPut(date) { MutableStateFlow(emptyList()) }.value = items
    }

    override fun todayItems(
        date: String,
        cadence: RoutineCadence,
    ): Flow<List<TodayRoutineItem>> =
        itemsByDate.getOrPut(date) { MutableStateFlow(emptyList()) }

    override fun summaryNote(date: String): Flow<String?> =
        summaryNotesByDate.map { notes -> notes[date] }

    fun setSummaryNote(
        date: String,
        note: String,
    ) {
        summaryNotesByDate.value = summaryNotesByDate.value + (date to note)
    }

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

    override suspend fun updateCompletedCount(
        date: String,
        routineItemId: Long,
        completedCount: Int,
    ) {
        countChanges += CountChange(
            date = date,
            routineItemId = routineItemId,
            completedCount = completedCount,
        )
    }

    override suspend fun setHidden(
        date: String,
        routineItemId: Long,
        isHidden: Boolean,
    ) {
        hiddenChanges += HiddenChange(
            date = date,
            routineItemId = routineItemId,
            isHidden = isHidden,
        )
    }

    override suspend fun updateSummaryNote(
        date: String,
        note: String?,
    ) {
        summaryNoteChanges += SummaryNoteChange(date = date, note = note)
        summaryNotesByDate.value = if (note == null) {
            summaryNotesByDate.value - date
        } else {
            summaryNotesByDate.value + (date to note)
        }
    }

    override suspend fun resetDate(date: String) {
        resetDates += date
        summaryNotesByDate.value = summaryNotesByDate.value - date
    }
}

data class CheckedChange(
    val date: String,
    val routineItemId: Long,
    val isChecked: Boolean,
)

data class HiddenChange(
    val date: String,
    val routineItemId: Long,
    val isHidden: Boolean,
)

data class NoteChange(
    val date: String,
    val routineItemId: Long,
    val note: String?,
)

data class CountChange(
    val date: String,
    val routineItemId: Long,
    val completedCount: Int,
)

data class SummaryNoteChange(
    val date: String,
    val note: String?,
)
