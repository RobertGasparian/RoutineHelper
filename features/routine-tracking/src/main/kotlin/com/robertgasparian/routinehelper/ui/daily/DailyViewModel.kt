package com.robertgasparian.routinehelper.ui.daily

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalSource
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalUndoCoordinator
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReorderDailyRoutineItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.ReflectionTagsUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetTodayItemCheckedUseCase
import com.robertgasparian.routinehelper.domain.usecase.SetTodayItemHiddenUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodayItemsUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodayReflectionUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayItemCompletedCountUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayItemNoteUseCase
import com.robertgasparian.routinehelper.domain.usecase.TodayReflectionSaveCoordinator
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
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel
class DailyViewModel @Inject constructor(
    todayItemsUseCase: TodayItemsUseCase,
    todayReflectionUseCase: TodayReflectionUseCase,
    reflectionTagsUseCase: ReflectionTagsUseCase,
    private val debugItemsPopulator: RoutineTrackingDebugItemsPopulator,
    private val finalizeTodayUseCase: FinalizeTodayUseCase,
    private val routineRemovalUndoCoordinator: RoutineRemovalUndoCoordinator,
    private val reorderDailyRoutineItemsUseCase: ReorderDailyRoutineItemsUseCase,
    private val setTodayItemCheckedUseCase: SetTodayItemCheckedUseCase,
    private val setTodayItemHiddenUseCase: SetTodayItemHiddenUseCase,
    private val updateTodayItemCompletedCountUseCase: UpdateTodayItemCompletedCountUseCase,
    private val updateTodayItemNoteUseCase: UpdateTodayItemNoteUseCase,
    private val todayReflectionSaveCoordinator: TodayReflectionSaveCoordinator,
    private val noteDateTimeTextProvider: NoteDateTimeTextProvider,
    private val timeProvider: TimeProvider,
) : BaseViewModel<RoutineTrackingUiState, RoutineTrackingIntent, Nothing>() {
    private val todayDate = timeProvider.currentDate().toString()
    private val noteEditor = MutableStateFlow<NoteEditorUiState?>(null)

    override val uiState: StateFlow<RoutineTrackingUiState> =
        combine(
            todayItemsUseCase(todayDate),
            todayReflectionUseCase(todayDate),
            noteEditor,
            routineRemovalUndoCoordinator.state,
            reflectionTagsUseCase(RoutineCadence.Daily),
        ) { items, reflection, noteEditor, removalState, reflectionTags ->
            val selectedTagIds = reflection.selectedTags.mapNotNullTo(mutableSetOf()) { tag ->
                tag.templateTagId
            }
            RoutineTrackingUiState(
                date = todayDate,
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
                canRemoveItems = removalState.allowsRemovalFrom(RoutineRemovalSource.Daily),
            )
        }
            .stateInViewModel(initialValue = RoutineTrackingUiState(date = todayDate))

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
            RoutineTrackingIntent.SnapshotClick -> snapshotDaily()
            is RoutineTrackingIntent.SnapshotDateSelected -> snapshotDaily(snapshotDate = intent.date)
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
            setTodayItemCheckedUseCase(
                date = todayDate,
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
            setTodayItemHiddenUseCase(
                date = todayDate,
                routineItemId = routineItemId,
                isHidden = isHidden,
            )
        }
    }

    private fun reorderItems(routineItemIdsInOrder: List<Long>) {
        launch {
            reorderDailyRoutineItemsUseCase(routineItemIdsInOrder)
        }
    }

    private fun addTestItems() {
        val existingItemCount = uiState.value.items.size
        launch {
            debugItemsPopulator(
                cadence = RoutineCadence.Daily,
                existingItemCount = existingItemCount,
            )
        }
    }

    private fun removeItem(routineItemId: Long) {
        launch {
            routineRemovalUndoCoordinator.requestRemoval(
                source = RoutineRemovalSource.Daily,
                itemId = routineItemId,
            )
        }
    }

    private fun updateNote(
        routineItemId: Long,
        note: String,
    ) {
        launch {
            updateTodayItemNoteUseCase(
                date = todayDate,
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
            todayReflectionSaveCoordinator(
                date = todayDate,
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
            cadence = RoutineCadence.Daily,
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
            updateTodayItemCompletedCountUseCase(
                date = todayDate,
                routineItemId = routineItemId,
                completedCount = completedCount,
            )
        }
    }

    private fun snapshotDaily(
        // TODO Remove this test-only override when debug snapshot controls are removed.
        snapshotDate: String = SnapshotDates.dailySnapshotDate(timeProvider.now()).toString(),
    ) {
        launch {
            finalizeTodayUseCase(
                date = todayDate,
                snapshotPeriodStartDate = snapshotDate,
                finalizedAtMillis = timeProvider.currentTimeMillis(),
            )
        }
    }
}
