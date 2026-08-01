package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import javax.inject.Inject

class TodayReflectionUseCase @Inject constructor(
    private val todayRoutineRepository: TodayRoutineRepository,
) {
    operator fun invoke(date: String) = todayRoutineRepository.reflection(date)
}
