package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineDaySummary
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SnapshotSummariesUseCase @Inject constructor(
    private val routineHistoryRepository: RoutineHistoryRepository,
) {
    operator fun invoke(): Flow<List<RoutineDaySummary>> =
        routineHistoryRepository.snapshotSummaries()
}
