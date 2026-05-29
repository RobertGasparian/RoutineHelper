package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject

class UpdateTodayItemNoteUseCase @Inject constructor(
    private val todayRoutineRepository: TodayRoutineRepository,
) {
    suspend operator fun invoke(
        date: String,
        routineItemId: Long,
        note: String,
    ) {
        todayRoutineRepository.updateNote(
            date = date,
            routineItemId = routineItemId,
            note = note,
        )
    }
}
