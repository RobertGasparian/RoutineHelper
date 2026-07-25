package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import javax.inject.Inject

/**
 * Intentional command boundary for summary edits.
 *
 * Keep this use case even while editing is universally allowed; future authorization or lifecycle
 * rules should be enforced here before the field-specific repository update is invoked.
 */
class UpdateSnapshotSummaryNoteUseCase @Inject constructor(
    private val routineHistoryRepository: RoutineHistoryRepository,
) {
    suspend operator fun invoke(
        snapshotId: Long,
        summaryNote: String?,
    ) {
        routineHistoryRepository.updateSnapshotSummaryNote(
            snapshotId = snapshotId,
            summaryNote = summaryNote,
        )
    }
}
