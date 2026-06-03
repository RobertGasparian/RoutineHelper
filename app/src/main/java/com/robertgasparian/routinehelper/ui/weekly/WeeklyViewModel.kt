package com.robertgasparian.routinehelper.ui.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemCompletedCountUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklySummaryNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklyItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklySummaryNoteUseCase
import com.robertgasparian.routinehelper.ui.daily.DailyItemUiState
import com.robertgasparian.routinehelper.ui.daily.DailyUiState
import com.robertgasparian.routinehelper.work.SnapshotWorkDates
import com.robertgasparian.routinehelper.work.startOfCalendarWeek
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WeeklyViewModel @Inject constructor(
    weeklyItemsUseCase: WeeklyItemsUseCase,
    weeklySummaryNoteUseCase: WeeklySummaryNoteUseCase,
    private val finalizeWeeklyUseCase: FinalizeWeeklyUseCase,
    private val setWeeklyItemCheckedUseCase: SetWeeklyItemCheckedUseCase,
    private val updateWeeklyItemCompletedCountUseCase: UpdateWeeklyItemCompletedCountUseCase,
    private val updateWeeklyItemNoteUseCase: UpdateWeeklyItemNoteUseCase,
    private val updateWeeklySummaryNoteUseCase: UpdateWeeklySummaryNoteUseCase,
) : ViewModel() {
    private val weekStartDate = LocalDate.now().startOfWeek().toString()

    val uiState: StateFlow<DailyUiState> =
        combine(
            weeklyItemsUseCase(weekStartDate),
            weeklySummaryNoteUseCase(weekStartDate),
        ) { items, summaryNote ->
            DailyUiState(
                date = "Week of $weekStartDate",
                summaryNote = summaryNote.orEmpty(),
                items = items.map(WeeklyRoutineItem::toUiState),
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DailyUiState(date = "Week of $weekStartDate"),
            )

    fun setChecked(
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        viewModelScope.launch {
            setWeeklyItemCheckedUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                isChecked = isChecked,
            )
        }
    }

    fun updateNote(
        routineItemId: Long,
        note: String,
    ) {
        viewModelScope.launch {
            updateWeeklyItemNoteUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                note = note,
            )
        }
    }

    fun updateSummaryNote(note: String) {
        viewModelScope.launch {
            updateWeeklySummaryNoteUseCase(
                weekStartDate = weekStartDate,
                note = note,
            )
        }
    }

    fun updateCompletedCount(
        routineItemId: Long,
        completedCount: Int,
    ) {
        viewModelScope.launch {
            updateWeeklyItemCompletedCountUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                completedCount = completedCount,
            )
        }
    }

    fun snapshotWeek() {
        viewModelScope.launch {
            val snapshotWeekStartDate = SnapshotWorkDates
                .previousCompletedCalendarWeekStartDate(ZonedDateTime.now())
                .toString()
            finalizeWeeklyUseCase(
                weekStartDate = snapshotWeekStartDate,
                snapshotWeekStartDate = snapshotWeekStartDate,
                finalizedAtMillis = System.currentTimeMillis(),
            )
        }
    }
}

private fun LocalDate.startOfWeek(): LocalDate {
    return startOfCalendarWeek()
}

private fun WeeklyRoutineItem.toUiState(): DailyItemUiState =
    DailyItemUiState(
        routineItemId = routineItemId,
        actionId = actionId,
        title = title,
        description = description,
        repeatTargetCount = repeatTargetCount,
        completedCount = completedCount,
        isChecked = isChecked,
        note = note.orEmpty(),
    )
