package com.robertgasparian.routinehelper.ui.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklyItemsUseCase
import com.robertgasparian.routinehelper.ui.today.TodayItemUiState
import com.robertgasparian.routinehelper.ui.today.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WeeklyViewModel @Inject constructor(
    weeklyItemsUseCase: WeeklyItemsUseCase,
    private val setWeeklyItemCheckedUseCase: SetWeeklyItemCheckedUseCase,
    private val updateWeeklyItemNoteUseCase: UpdateWeeklyItemNoteUseCase,
) : ViewModel() {
    private val weekStartDate = LocalDate.now().startOfWeek().toString()

    val uiState: StateFlow<TodayUiState> =
        weeklyItemsUseCase(weekStartDate)
            .map { items ->
                TodayUiState(
                    date = "Week of $weekStartDate",
                    items = items.map(WeeklyRoutineItem::toUiState),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TodayUiState(date = "Week of $weekStartDate"),
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
}

private fun LocalDate.startOfWeek(): LocalDate {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    return with(java.time.temporal.TemporalAdjusters.previousOrSame(firstDayOfWeek))
}

private fun WeeklyRoutineItem.toUiState(): TodayItemUiState =
    TodayItemUiState(
        routineItemId = routineItemId,
        actionId = actionId,
        title = title,
        description = description,
        isChecked = isChecked,
        note = note.orEmpty(),
    )
