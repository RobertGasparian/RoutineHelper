package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineDaySnapshot
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SnapshotUseCase @Inject constructor(
    private val routineHistoryRepository: RoutineHistoryRepository,
) {
    operator fun invoke(snapshotId: Long): Flow<RoutineDaySnapshot?> =
        routineHistoryRepository.snapshot(snapshotId)
}
