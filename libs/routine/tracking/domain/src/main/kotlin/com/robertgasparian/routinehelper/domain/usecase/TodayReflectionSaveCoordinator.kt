package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineReflection

fun interface TodayReflectionSaveCoordinator {
    suspend operator fun invoke(
        date: String,
        reflection: RoutineReflection,
        originalTagIds: Set<Long>,
        tagDraft: List<ReflectionTagTemplateDraft>,
    )
}
