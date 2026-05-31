package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import javax.inject.Inject

class UpdateWeeklySummaryNoteUseCase @Inject constructor(
    private val weeklyRoutineRepository: WeeklyRoutineRepository,
) {
    suspend operator fun invoke(
        weekStartDate: String,
        note: String?,
    ) {
        weeklyRoutineRepository.updateSummaryNote(weekStartDate = weekStartDate, note = note)
    }
}
