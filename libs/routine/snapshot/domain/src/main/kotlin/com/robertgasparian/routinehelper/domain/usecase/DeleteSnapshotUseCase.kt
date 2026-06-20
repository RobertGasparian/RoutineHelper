package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import javax.inject.Inject

class DeleteSnapshotUseCase @Inject constructor(
    private val routineHistoryRepository: RoutineHistoryRepository,
) {
    suspend operator fun invoke(snapshotId: Long) {
        routineHistoryRepository.deleteSnapshot(snapshotId)
    }
}
