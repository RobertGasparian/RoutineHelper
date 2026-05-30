package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRoutineTemplateRepository : RoutineTemplateRepository {
    private val items = MutableStateFlow<List<RoutineTemplateItem>>(emptyList())
    val addedItems = mutableListOf<AddedTemplateItem>()
    val updatedItems = mutableListOf<UpdatedTemplateItem>()

    fun setItems(items: List<RoutineTemplateItem>) {
        this.items.value = items
    }

    override fun templateItems(cadence: RoutineCadence): Flow<List<RoutineTemplateItem>> =
        items.map { templateItems -> templateItems.filter { it.cadence == cadence } }

    override fun templateItem(actionId: Long): Flow<RoutineTemplateItem?> =
        items.map { templateItems ->
            templateItems.firstOrNull { it.actionId == actionId }
        }

    override suspend fun addTemplateItem(
        title: String,
        description: String?,
        cadence: RoutineCadence,
    ): Long {
        addedItems += AddedTemplateItem(title = title, description = description, cadence = cadence)
        return addedItems.size.toLong()
    }

    override suspend fun updateAction(
        actionId: Long,
        title: String,
        description: String?,
    ) {
        updatedItems += UpdatedTemplateItem(
            actionId = actionId,
            title = title,
            description = description,
        )
    }

    override suspend fun removeTemplateItem(routineItemId: Long) = Unit

    override suspend fun reorderTemplateItems(routineItemIdsInOrder: List<Long>) = Unit
}

data class AddedTemplateItem(
    val title: String,
    val description: String?,
    val cadence: RoutineCadence,
)

data class UpdatedTemplateItem(
    val actionId: Long,
    val title: String,
    val description: String?,
)
