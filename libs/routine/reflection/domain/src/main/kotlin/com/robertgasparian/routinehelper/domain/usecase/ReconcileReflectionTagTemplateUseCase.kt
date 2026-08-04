package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import com.robertgasparian.routinehelper.domain.repository.ReflectionTagTemplateRepository
import javax.inject.Inject

/**
 * Applies one editor draft to a cadence's reusable tag template and resolves its selection.
 *
 * Only tags that were present when editing started can be deleted. This prevents a concurrent tag
 * addition from another observer from being removed merely because it was absent from this draft.
 */
class ReconcileReflectionTagTemplateUseCase @Inject constructor(
    private val repository: ReflectionTagTemplateRepository,
) {
    suspend operator fun invoke(
        cadence: RoutineCadence,
        originalTagIds: Set<Long>,
        draft: List<ReflectionTagTemplateDraft>,
    ): List<SelectedReflectionTag> =
        repository.reconcile(
            cadence = cadence,
            originalTagIds = originalTagIds,
            draft = draft,
        )
}
