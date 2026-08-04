package com.robertgasparian.routinehelper.data.coordinator

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.usecase.ReconcileReflectionTagTemplateUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyReflectionUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklyReflectionSaveCoordinator
import javax.inject.Inject

class RoomWeeklyReflectionSaveCoordinator @Inject constructor(
    private val database: RoomDatabase,
    private val reconcileReflectionTagTemplateUseCase: ReconcileReflectionTagTemplateUseCase,
    private val updateWeeklyReflectionUseCase: UpdateWeeklyReflectionUseCase,
) : WeeklyReflectionSaveCoordinator {
    override suspend fun invoke(
        weekStartDate: String,
        reflection: RoutineReflection,
        originalTagIds: Set<Long>,
        tagDraft: List<ReflectionTagTemplateDraft>,
    ) = database.withTransaction {
        val selectedTags = reconcileReflectionTagTemplateUseCase(
            cadence = RoutineCadence.Weekly,
            originalTagIds = originalTagIds,
            draft = tagDraft,
        )
        updateWeeklyReflectionUseCase(
            weekStartDate = weekStartDate,
            reflection = reflection.copy(selectedTags = selectedTags),
        )
    }
}
