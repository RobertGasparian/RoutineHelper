package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SnapshotSummariesUseCase @Inject constructor(
    private val routineHistoryRepository: RoutineHistoryRepository,
) {
    operator fun invoke(cadence: RoutineCadence? = null): Flow<List<RoutineSnapshotSummary>> =
        routineHistoryRepository.snapshotSummaries(cadence)
}
