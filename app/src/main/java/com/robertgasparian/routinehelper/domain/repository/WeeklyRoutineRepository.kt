package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import kotlinx.coroutines.flow.Flow

interface WeeklyRoutineRepository {
    fun weeklyItems(weekStartDate: String): Flow<List<WeeklyRoutineItem>>

    suspend fun setChecked(
        weekStartDate: String,
        routineItemId: Long,
        isChecked: Boolean,
    )

    suspend fun updateNote(
        weekStartDate: String,
        routineItemId: Long,
        note: String?,
    )

    suspend fun updateCompletedCount(
        weekStartDate: String,
        routineItemId: Long,
        completedCount: Int,
    )

    suspend fun resetWeek(weekStartDate: String)
}
