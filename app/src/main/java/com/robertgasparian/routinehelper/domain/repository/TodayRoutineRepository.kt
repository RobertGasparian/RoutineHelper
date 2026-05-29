package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import kotlinx.coroutines.flow.Flow

interface TodayRoutineRepository {
    fun todayItems(date: String): Flow<List<TodayRoutineItem>>

    suspend fun setChecked(
        date: String,
        routineItemId: Long,
        isChecked: Boolean,
    )

    suspend fun updateNote(
        date: String,
        routineItemId: Long,
        note: String?,
    )

    suspend fun resetDate(date: String)
}
