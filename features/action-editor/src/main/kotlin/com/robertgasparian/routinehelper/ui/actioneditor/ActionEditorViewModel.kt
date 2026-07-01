package com.robertgasparian.routinehelper.ui.actioneditor

import com.robertgasparian.routinehelper.core.presentation.BaseViewModel
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import com.robertgasparian.routinehelper.domain.usecase.AddTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.RemoveTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.TemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTemplateItemUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach

@HiltViewModel(assistedFactory = ActionEditorViewModel.Factory::class)
class ActionEditorViewModel @AssistedInject constructor(
    @Assisted private val actionId: Long?,
    @Assisted private val cadence: RoutineCadence,
    private val addTemplateItemUseCase: AddTemplateItemUseCase,
    private val removeTemplateItemUseCase: RemoveTemplateItemUseCase,
    private val templateItemUseCase: TemplateItemUseCase,
    private val updateTemplateItemUseCase: UpdateTemplateItemUseCase,
) : BaseViewModel<ActionEditorUiState, ActionEditorIntent, ActionEditorUiEvent>() {
    private val draftTitle = MutableStateFlow("")
    private val draftDescription = MutableStateFlow("")
    private val isRepeatEnabled = MutableStateFlow(false)
    private val repeatTargetCount = MutableStateFlow(2)
    private var isTemplateItemLoaded = false

    private val templateItem: Flow<RoutineTemplateItem?> =
        if (actionId == null) {
            flowOf(null)
        } else {
            templateItemUseCase(actionId).onEach { item ->
                if (item != null && !isTemplateItemLoaded) {
                    isTemplateItemLoaded = true
                    loadDraft(item)
                }
            }
        }

    override val uiState: StateFlow<ActionEditorUiState> =
        combine(
            templateItem,
            draftTitle,
            draftDescription,
            isRepeatEnabled,
            repeatTargetCount,
        ) { _, title, description, isRepeatEnabled, repeatTargetCount ->
            ActionEditorUiState(
                title = title,
                description = description,
                isRepeatEnabled = isRepeatEnabled,
                repeatTargetCount = repeatTargetCount,
                isEditing = actionId != null,
            )
        }
            .distinctUntilChanged()
            .stateInViewModel(initialValue = ActionEditorUiState(isEditing = actionId != null))

    override fun handleIntent(intent: ActionEditorIntent) {
        when (intent) {
            ActionEditorIntent.BackClick -> Unit
            ActionEditorIntent.DeleteClick -> delete()
            ActionEditorIntent.SaveClick -> save()
            is ActionEditorIntent.DescriptionChange -> updateDescription(intent.description)
            is ActionEditorIntent.RepeatEnabledChange -> updateRepeatEnabled(intent.enabled)
            is ActionEditorIntent.RepeatTargetCountChange -> updateRepeatTargetCount(intent.targetCount)
            is ActionEditorIntent.TitleChange -> updateTitle(intent.title)
        }
    }

    private fun updateTitle(title: String) {
        draftTitle.value = title
    }

    private fun updateDescription(description: String) {
        draftDescription.value = description
    }

    private fun updateRepeatEnabled(enabled: Boolean) {
        isRepeatEnabled.value = enabled
    }

    private fun updateRepeatTargetCount(targetCount: Int) {
        repeatTargetCount.value = targetCount.coerceAtLeast(2)
    }

    private fun save() {
        launch {
            val targetCount = repeatTargetCount.value.takeIf { isRepeatEnabled.value }
            if (actionId == null) {
                addTemplateItemUseCase(
                    title = draftTitle.value,
                    description = draftDescription.value,
                    repeatTargetCount = targetCount,
                    cadence = cadence,
                )
            } else {
                updateTemplateItemUseCase(
                    actionId = actionId,
                    title = draftTitle.value,
                    description = draftDescription.value,
                    repeatTargetCount = targetCount,
                )
            }
            emitUiEvent(ActionEditorUiEvent.Saved)
        }
    }

    private fun delete() {
        if (actionId == null) return

        launch {
            val item = templateItemUseCase(actionId).first() ?: return@launch
            removeTemplateItemUseCase(item.routineItemId)
            emitUiEvent(ActionEditorUiEvent.Deleted)
        }
    }

    private fun loadDraft(item: RoutineTemplateItem) {
        draftTitle.value = item.title
        draftDescription.value = item.description.orEmpty()
        isRepeatEnabled.value = item.repeatTargetCount != null
        repeatTargetCount.value = item.repeatTargetCount ?: 2
    }

    @AssistedFactory
    interface Factory {
        fun create(
            actionId: Long?,
            cadence: RoutineCadence,
        ): ActionEditorViewModel
    }
}
