package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject

class UpdateTodaySummaryNoteUseCase @Inject constructor(
    private val todayRoutineRepository: TodayRoutineRepository,
) {
    suspend operator fun invoke(
        date: String,
        note: String?,
    ) {
        todayRoutineRepository.updateSummaryNote(date = date, note = note)
    }
}
