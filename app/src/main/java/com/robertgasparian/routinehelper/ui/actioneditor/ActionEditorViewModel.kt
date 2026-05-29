package com.robertgasparian.routinehelper.ui.actioneditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertgasparian.routinehelper.domain.usecase.SaveTemplateItemUseCase
import com.robertgasparian.routinehelper.domain.usecase.TemplateItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class ActionEditorViewModel @Inject constructor(
    private val saveTemplateItemUseCase: SaveTemplateItemUseCase,
    private val templateItemUseCase: TemplateItemUseCase,
) : ViewModel() {
    private val draftTitle = MutableStateFlow("")
    private val draftDescription = MutableStateFlow("")
    private val loadedActionIds = mutableSetOf<Long>()

    fun uiState(actionId: Long?): Flow<ActionEditorUiState> {
        val source = if (actionId == null) {
            MutableStateFlow(null)
        } else {
            templateItemUseCase(actionId).onEach { item ->
                if (item != null && loadedActionIds.add(actionId)) {
                    draftTitle.value = item.title
                    draftDescription.value = item.description.orEmpty()
                }
            }
        }

        return combine(
            source,
            draftTitle,
            draftDescription,
        ) { _, title, description ->
            ActionEditorUiState(
                title = title,
                description = description,
                isEditing = actionId != null,
            )
        }.distinctUntilChanged()
    }

    fun updateTitle(title: String) {
        draftTitle.value = title
    }

    fun updateDescription(description: String) {
        draftDescription.value = description
    }

    fun save(
        actionId: Long?,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            saveTemplateItemUseCase(
                actionId = actionId,
                title = draftTitle.value,
                description = draftDescription.value,
            )
            onSaved()
        }
    }
}
