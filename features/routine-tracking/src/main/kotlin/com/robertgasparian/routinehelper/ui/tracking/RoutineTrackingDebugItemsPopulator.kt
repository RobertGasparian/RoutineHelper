package com.robertgasparian.routinehelper.ui.tracking

import android.content.Context
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.usecase.AddTemplateItemUseCase
import com.robertgasparian.routinehelper.features.routinetracking.R
import dagger.Binds
import dagger.Module
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class RoutineTrackingDebugItemsPopulator @Inject constructor(
    private val addTemplateItemUseCase: AddTemplateItemUseCase,
    private val debugTextProvider: RoutineTrackingDebugTextProvider,
) {
    suspend operator fun invoke(
        cadence: RoutineCadence,
        existingItemCount: Int,
    ) {
        val firstItemNumber = existingItemCount + 1

        repeat(DebugItemBatchSize) { offset ->
            val itemNumber = firstItemNumber + offset
            val itemText = debugTextProvider.itemText(cadence, itemNumber)
            addTemplateItemUseCase(
                title = itemText.title,
                description = if (itemNumber % 2 == 0) {
                    itemText.description
                } else {
                    null
                },
                cadence = cadence,
            )
        }
    }
}

fun interface RoutineTrackingDebugTextProvider {
    fun itemText(cadence: RoutineCadence, itemNumber: Int): RoutineTrackingDebugItemText
}

data class RoutineTrackingDebugItemText(
    val title: String,
    val description: String,
)

class AndroidRoutineTrackingDebugTextProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RoutineTrackingDebugTextProvider {
    override fun itemText(cadence: RoutineCadence, itemNumber: Int): RoutineTrackingDebugItemText {
        val actionLabel = context.getString(cadence.debugActionLabelRes)
        return RoutineTrackingDebugItemText(
            title = context.getString(
                R.string.routine_tracking_debug_item_title,
                actionLabel,
                itemNumber,
            ),
            description = context.getString(
                R.string.routine_tracking_debug_item_description,
                actionLabel,
                itemNumber,
            ),
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutineTrackingDebugTextProviderModule {
    @Binds
    abstract fun bindRoutineTrackingDebugTextProvider(
        provider: AndroidRoutineTrackingDebugTextProvider,
    ): RoutineTrackingDebugTextProvider
}

private val RoutineCadence.debugActionLabelRes: Int
    get() = when (this) {
        RoutineCadence.Daily -> R.string.routine_tracking_debug_daily_action
        RoutineCadence.Weekly -> R.string.routine_tracking_debug_weekly_action
    }

private const val DebugItemBatchSize = 20
