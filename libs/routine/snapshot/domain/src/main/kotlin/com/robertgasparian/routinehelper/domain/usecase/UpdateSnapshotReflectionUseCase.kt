package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import javax.inject.Inject

/**
 * Intentional command boundary for reflection edits.
 *
 * Keep this use case even while editing is universally allowed; future authorization or lifecycle
 * rules should be enforced here before the field-specific repository update is invoked.
 */
class UpdateSnapshotReflectionUseCase @Inject constructor(
    private val routineHistoryRepository: RoutineHistoryRepository,
) {
    suspend operator fun invoke(
        snapshotId: Long,
        reflection: RoutineReflection,
    ) {
        routineHistoryRepository.updateSnapshotReflection(
            snapshotId = snapshotId,
            reflection = reflection,
        )
    }
}
