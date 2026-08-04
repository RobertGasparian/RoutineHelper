package com.robertgasparian.routinehelper.ui.weekly

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.core.time.startOfCalendarWeek
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalUndoCoordinator
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderWeeklyRoutineItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReflectionTagsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetWeeklyItemHiddenUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemCompletedCountUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklyReflectionSaveCoordinator
import com.robertgasparian.routinehelper.domain.usecase.WeeklyItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.WeeklyReflectionUseCase
import com.robertgasparian.routinehelper.ui.dsm.RoutineNoteDraftUiState
import com.robertgasparian.routinehelper.ui.dsm.insertAtCursor
import com.robertgasparian.routinehelper.ui.reflection.api.ReflectionEditorTag
import com.robertgasparian.routinehelper.ui.tracking.NoteDateTimeTextProvider
import com.robertgasparian.routinehelper.ui.tracking.NoteEditorTarget
import com.robertgasparian.routinehelper.ui.tracking.NoteEditorUiState
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingDebugItemsPopulator
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingIntent
import com.robertgasparian.routinehelper.ui.tracking.RoutineTrackingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel
class WeeklyViewModel @Inject constructor(
    weeklyItemsUseCase: WeeklyItemsUseCase,
    weeklyReflectionUseCase: WeeklyReflectionUseCase,
    reflectionTagsUseCase: ReflectionTagsUseCase,
    private val debugItemsPopulator: RoutineTrackingDebugItemsPopulator,
    private val finalizeWeeklyUseCase: FinalizeWeeklyUseCase,
    private val routineRemovalUndoCoordinator: RoutineRemovalUndoCoordinator,
    private val reorderWeeklyRoutineItemsUseCase: ReorderWeeklyRoutineItemsUseCase,
    private val setWeeklyItemCheckedUseCase: SetWeeklyItemCheckedUseCase,
    private val setWeeklyItemHiddenUseCase: SetWeeklyItemHiddenUseCase,
    private val updateWeeklyItemCompletedCountUseCase: UpdateWeeklyItemCompletedCountUseCase,
    private val updateWeeklyItemNoteUseCase: UpdateWeeklyItemNoteUseCase,
    private val weeklyReflectionSaveCoordinator: WeeklyReflectionSaveCoordinator,
    private val noteDateTimeTextProvider: NoteDateTimeTextProvider,
    private val timeProvider: TimeProvider,
) : BaseViewModel<RoutineTrackingUiState, RoutineTrackingIntent, Nothing>() {
    private val weekStartDate = timeProvider.currentDate().startOfWeek().toString()
    private val noteEditor = MutableStateFlow<NoteEditorUiState?>(null)

    override val uiState: StateFlow<RoutineTrackingUiState> =
        combine(
            weeklyItemsUseCase(weekStartDate),
            weeklyReflectionUseCase(weekStartDate),
            noteEditor,
            routineRemovalUndoCoordinator.state,
            reflectionTagsUseCase(RoutineCadence.Weekly),
        ) { items, reflection, noteEditor, removalState, reflectionTags ->
            val selectedTagIds = reflection.selectedTags.mapNotNullTo(mutableSetOf()) { tag ->
                tag.templateTagId
            }
            RoutineTrackingUiState(
                date = weekStartDate,
                summaryNote = reflection.summaryNote.orEmpty(),
                rating = reflection.rating,
                reflectionTags = reflectionTags.map { tag ->
                    ReflectionEditorTag(
                        sourceId = tag.id,
                        label = tag.label,
                        isSelected = tag.id in selectedTagIds,
                    )
                },
                items = items.map { item -> item.toRoutineTrackingItemUiState() },
                noteEditor = noteEditor,
                canRemoveItems = removalState.allowsRemovalFrom(RoutineRemovalSource.Weekly),
            )
        }
            .stateInViewModel(initialValue = RoutineTrackingUiState(date = weekStartDate))

    override fun handleIntent(intent: RoutineTrackingIntent) {
        when (intent) {
            RoutineTrackingIntent.CreateActionClick,
            RoutineTrackingIntent.SettingsClick,
            is RoutineTrackingIntent.EditActionClick -> Unit
            RoutineTrackingIntent.AddTestItemsClick -> addTestItems()
            is RoutineTrackingIntent.CheckedChange -> setChecked(
                routineItemId = intent.routineItemId,
                isChecked = intent.isChecked,
            )
            is RoutineTrackingIntent.CompletedCountChange -> updateCompletedCount(
                routineItemId = intent.routineItemId,
                completedCount = intent.completedCount,
            )
            is RoutineTrackingIntent.HiddenChange -> setHidden(
                routineItemId = intent.routineItemId,
                isHidden = intent.isHidden,
            )
            is RoutineTrackingIntent.RemoveItem -> removeItem(intent.routineItemId)
            is RoutineTrackingIntent.ReorderItems -> reorderItems(intent.routineItemIdsInOrder)
            RoutineTrackingIntent.SnapshotClick -> snapshotWeek()
            is RoutineTrackingIntent.SnapshotDateSelected -> snapshotWeek(snapshotWeekStartDate = intent.date)
            is RoutineTrackingIntent.EditNoteClick -> showItemNoteEditor(
                routineItemId = intent.routineItemId,
                note = intent.note,
                itemTitle = intent.itemTitle,
            )
            RoutineTrackingIntent.EditReflectionClick -> Unit
            is RoutineTrackingIntent.SaveReflection -> updateReflection(
                summaryNote = intent.summaryNote,
                rating = intent.rating,
                originalTags = intent.originalTags,
                tags = intent.tags,
            )
            is RoutineTrackingIntent.NoteDraftChange -> updateNoteDraft(intent)
            RoutineTrackingIntent.NoteDraftClearClick -> clearNoteDraft()
            RoutineTrackingIntent.NoteDraftDateClick -> insertCurrentDateIntoNoteDraft()
            RoutineTrackingIntent.NoteDraftWeekdayClick -> insertCurrentWeekdayIntoNoteDraft()
            RoutineTrackingIntent.NoteDraftTimeClick -> insertCurrentTimeIntoNoteDraft()
            RoutineTrackingIntent.NoteEditorDismiss -> dismissNoteEditor()
            RoutineTrackingIntent.NoteEditorSaveClick -> saveNoteDraft()
        }
    }

    private fun setChecked(
        routineItemId: Long,
        isChecked: Boolean,
    ) {
        launch {
            setWeeklyItemCheckedUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                isChecked = isChecked,
            )
        }
    }

    private fun setHidden(
        routineItemId: Long,
        isHidden: Boolean,
    ) {
        launch {
            setWeeklyItemHiddenUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                isHidden = isHidden,
            )
        }
    }

    private fun reorderItems(routineItemIdsInOrder: List<Long>) {
        launch {
            reorderWeeklyRoutineItemsUseCase(routineItemIdsInOrder)
        }
    }

    private fun addTestItems() {
        val existingItemCount = uiState.value.items.size
        launch {
            debugItemsPopulator(
                cadence = RoutineCadence.Weekly,
                existingItemCount = existingItemCount,
            )
        }
    }

    private fun removeItem(routineItemId: Long) {
        launch {
            routineRemovalUndoCoordinator.requestRemoval(
                source = RoutineRemovalSource.Weekly,
                itemId = routineItemId,
            )
        }
    }

    private fun updateNote(
        routineItemId: Long,
        note: String,
    ) {
        launch {
            updateWeeklyItemNoteUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                note = note,
            )
        }
    }

    private fun updateReflection(
        summaryNote: String,
        rating: ReflectionRating?,
        originalTags: List<ReflectionEditorTag>,
        tags: List<ReflectionEditorTag>,
    ) {
        launch {
            weeklyReflectionSaveCoordinator(
                weekStartDate = weekStartDate,
                reflection = RoutineReflection(
                    summaryNote = summaryNote,
                    rating = rating,
                ),
                originalTagIds = originalTags.mapNotNullTo(mutableSetOf(), ReflectionEditorTag::sourceId),
                tagDraft = tags.map { tag ->
                    ReflectionTagTemplateDraft(
                        sourceTagId = tag.sourceId,
                        label = tag.label,
                        isSelected = tag.isSelected,
                    )
                },
            )
        }
    }

    private fun showItemNoteEditor(
        routineItemId: Long,
        note: String,
        itemTitle: String,
    ) {
        noteEditor.value = NoteEditorUiState.item(
            routineItemId = routineItemId,
            note = note,
            cadence = RoutineCadence.Weekly,
            itemTitle = itemTitle,
        )
    }

    private fun updateNoteDraft(intent: RoutineTrackingIntent.NoteDraftChange) {
        noteEditor.value = noteEditor.value?.copy(
            value = RoutineNoteDraftUiState(
                text = intent.text,
                selectionStart = intent.selectionStart,
                selectionEnd = intent.selectionEnd,
            ),
        )
    }

    private fun insertCurrentDateIntoNoteDraft() {
        insertTextIntoNoteDraft(noteDateTimeTextProvider.currentDateText())
    }

    private fun insertCurrentWeekdayIntoNoteDraft() {
        insertTextIntoNoteDraft(noteDateTimeTextProvider.currentWeekdayText())
    }

    private fun insertCurrentTimeIntoNoteDraft() {
        insertTextIntoNoteDraft(noteDateTimeTextProvider.currentTimeText())
    }

    private fun clearNoteDraft() {
        noteEditor.value = noteEditor.value?.copy(value = RoutineNoteDraftUiState.fromText(""))
    }

    private fun dismissNoteEditor() {
        noteEditor.value = null
    }

    private fun saveNoteDraft() {
        val editor = noteEditor.value ?: return
        val target = editor.target
        updateNote(
            routineItemId = target.routineItemId,
            note = editor.value.text,
        )
        noteEditor.value = null
    }

    private fun insertTextIntoNoteDraft(text: String) {
        noteEditor.value = noteEditor.value?.let { editor ->
            editor.copy(value = editor.value.insertAtCursor(text))
        }
    }

    private fun updateCompletedCount(
        routineItemId: Long,
        completedCount: Int,
    ) {
        launch {
            updateWeeklyItemCompletedCountUseCase(
                weekStartDate = weekStartDate,
                routineItemId = routineItemId,
                completedCount = completedCount,
            )
        }
    }

    private fun snapshotWeek(
        // TODO Remove this test-only override when debug snapshot controls are removed.
        snapshotWeekStartDate: String = SnapshotDates
            .previousCompletedCalendarWeekStartDate(timeProvider.now())
            .toString(),
    ) {
        launch {
            finalizeWeeklyUseCase(
                weekStartDate = weekStartDate,
                snapshotPeriodStartDate = snapshotWeekStartDate,
                finalizedAtMillis = timeProvider.currentTimeMillis(),
            )
        }
    }
}

private fun LocalDate.startOfWeek(): LocalDate {
    return startOfCalendarWeek()
}
