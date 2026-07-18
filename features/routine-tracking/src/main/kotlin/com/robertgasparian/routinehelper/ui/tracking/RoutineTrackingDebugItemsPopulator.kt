package com.robertgasparian.routinehelper.ui.tracking

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.usecase.AddTemplateItemUseCase
import javax.inject.Inject

class RoutineTrackingDebugItemsPopulator @Inject constructor(
    private val addTemplateItemUseCase: AddTemplateItemUseCase,
) {
    suspend operator fun invoke(
        cadence: RoutineCadence,
        existingItemCount: Int,
    ) {
        val actionLabel = cadence.debugActionLabel
        val firstItemNumber = existingItemCount + 1

        repeat(DebugItemBatchSize) { offset ->
            val itemNumber = firstItemNumber + offset
            addTemplateItemUseCase(
                title = "$actionLabel $itemNumber",
                description = if (itemNumber % 2 == 0) {
                    "description for $actionLabel $itemNumber"
                } else {
                    null
                },
                cadence = cadence,
            )
        }
    }
}

private val RoutineCadence.debugActionLabel: String
    get() = when (this) {
        RoutineCadence.Daily -> "daily action"
        RoutineCadence.Weekly -> "weekly action"
    }

private const val DebugItemBatchSize = 20
