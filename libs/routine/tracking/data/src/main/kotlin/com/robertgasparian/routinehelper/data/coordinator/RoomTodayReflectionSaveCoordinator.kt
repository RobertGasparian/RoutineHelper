package com.robertgasparian.routinehelper.data.coordinator

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.usecase.ReconcileReflectionTagTemplateUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodayReflectionSaveCoordinator
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayReflectionUseCase
import javax.inject.Inject

class RoomTodayReflectionSaveCoordinator @Inject constructor(
    private val database: RoomDatabase,
    private val reconcileReflectionTagTemplateUseCase: ReconcileReflectionTagTemplateUseCase,
    private val updateTodayReflectionUseCase: UpdateTodayReflectionUseCase,
) : TodayReflectionSaveCoordinator {
    override suspend fun invoke(
        date: String,
        reflection: RoutineReflection,
        originalTagIds: Set<Long>,
        tagDraft: List<ReflectionTagTemplateDraft>,
    ) = database.withTransaction {
        val selectedTags = reconcileReflectionTagTemplateUseCase(
            cadence = RoutineCadence.Daily,
            originalTagIds = originalTagIds,
            draft = tagDraft,
        )
        updateTodayReflectionUseCase(
            date = date,
            reflection = reflection.copy(selectedTags = selectedTags),
        )
    }
}
