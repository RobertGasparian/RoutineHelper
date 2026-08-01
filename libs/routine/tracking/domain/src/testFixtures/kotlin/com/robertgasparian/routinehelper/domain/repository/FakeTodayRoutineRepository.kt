package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTodayRoutineRepository : TodayRoutineRepository {
    private val itemsByDate = mutableMapOf<String, MutableStateFlow<List<TodayRoutineItem>>>()
    private val reflectionsByDate = MutableStateFlow<Map<String, RoutineReflection>>(emptyMap())
    val checkedChanges = mutableListOf<CheckedChange>()
    val hiddenChanges = mutableListOf<HiddenChange>()
    val noteChanges = mutableListOf<NoteChange>()
    val countChanges = mutableListOf<CountChange>()
    val reflectionChanges = mutableListOf<ReflectionChange>()
    val resetDates = mutableListOf<String>()

    fun setItems(
        date: String,
        items: List<TodayRoutineItem>,
    ) {
        itemsByDate.getOrPut(date) { MutableStateFlow(emptyList()) }.value = items
    }

    override fun todayItems(date: String): Flow<List<TodayRoutineItem>> =
        itemsByDate.getOrPut(date) { MutableStateFlow(emptyList()) }

    override fun reflection(date: String): Flow<RoutineReflection> =
        reflectionsByDate.map { reflections -> reflections[date] ?: RoutineReflection() }

    fun setSummaryNote(
        date: String,
        note: String,
    ) {
        setReflection(
            date = date,
            reflection = RoutineReflection(summaryNote = note),
        )
    }

    fun setReflection(
        date: String,
        reflection: RoutineReflection,
    ) {
        reflectionsByDate.value = reflectionsByDate.value + (date to reflection)
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

    override suspend fun updateReflection(
        date: String,
        reflection: RoutineReflection,
    ) {
        reflectionChanges += ReflectionChange(date = date, reflection = reflection)
        reflectionsByDate.value = if (reflection.isEmpty) {
            reflectionsByDate.value - date
        } else {
            reflectionsByDate.value + (date to reflection)
        }
    }

    override suspend fun resetDate(date: String) {
        resetDates += date
        reflectionsByDate.value = reflectionsByDate.value - date
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

data class ReflectionChange(
    val date: String,
    val reflection: RoutineReflection,
)
