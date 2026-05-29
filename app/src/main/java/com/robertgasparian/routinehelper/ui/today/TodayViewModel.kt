package com.robertgasparian.routinehelper.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.usecase.AddTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetTodayItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodayItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayItemNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TodayViewModel @Inject constructor(
    todayItemsUseCase: TodayItemsUseCase,
    private val addTemplateItemUseCase: AddTemplateItemUseCase,
    private val setTodayItemCheckedUseCase: SetTodayItemCheckedUseCase,
    private val updateTodayItemNoteUseCase: UpdateTodayItemNoteUseCase,
) : ViewModel() {
    private val todayDate = LocalDate.now().toString()

    val uiState: StateFlow<TodayUiState> =
        todayItemsUseCase(todayDate)
            .map { items ->
                TodayUiState(
                    date = todayDate,
                    items = items.map(TodayRoutineItem::toUiState),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TodayUiState(date = todayDate),
            )

    fun addAction(
        title: String,
        description: String?,
    ) {
        viewModelScope.launch {
            addTemplateItemUseCase(
                title = title,
                description = description,
            )
        }
    }

    fun setChecked(
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        viewModelScope.launch {
            setTodayItemCheckedUseCase(
                date = todayDate,
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
            updateTodayItemNoteUseCase(
                date = todayDate,
                routineItemId = routineItemId,
                note = note,
            )
        }
    }
}

private fun TodayRoutineItem.toUiState(): TodayItemUiState =
    TodayItemUiState(
        routineItemId = routineItemId,
        title = title,
        description = description,
        isChecked = isChecked,
        note = note.orEmpty(),
    )
