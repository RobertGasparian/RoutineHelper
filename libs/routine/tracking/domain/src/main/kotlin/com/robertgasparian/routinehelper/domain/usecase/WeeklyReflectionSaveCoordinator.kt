package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineReflection

fun interface WeeklyReflectionSaveCoordinator {
    suspend operator fun invoke(
        weekStartDate: String,
        reflection: RoutineReflection,
        originalTagIds: Set<Long>,
        tagDraft: List<ReflectionTagTemplateDraft>,
    )
}
