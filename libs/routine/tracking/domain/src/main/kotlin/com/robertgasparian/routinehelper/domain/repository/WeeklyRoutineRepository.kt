package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import kotlinx.coroutines.flow.Flow

interface WeeklyRoutineRepository {
    fun weeklyItems(weekStartDate: String): Flow<List<WeeklyRoutineItem>>

    fun reflection(weekStartDate: String): Flow<RoutineReflection>

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

    suspend fun setHidden(
        weekStartDate: String,
        routineItemId: Long,
        isHidden: Boolean,
    )

    suspend fun updateReflection(
        weekStartDate: String,
        reflection: RoutineReflection,
    )

    suspend fun resetWeek(weekStartDate: String)
}
