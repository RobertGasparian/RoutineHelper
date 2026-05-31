package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject

class WeeklySummaryNoteUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
) {
    operator fun invoke(weekStartDate: String) = weeklyRoutineRepository.summaryNote(weekStartDate)
}
