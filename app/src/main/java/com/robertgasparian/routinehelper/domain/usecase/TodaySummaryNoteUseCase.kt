package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject

class TodaySummaryNoteUseCase @Inject constructor(
    private val todayRoutineRepository: TodayRoutineRepository,
) {
    operator fun invoke(date: String) = todayRoutineRepository.summaryNote(date)
}
