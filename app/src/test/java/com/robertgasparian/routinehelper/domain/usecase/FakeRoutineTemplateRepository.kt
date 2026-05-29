package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.RoutineTemplateItem
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRoutineTemplateRepository : RoutineTemplateRepository {
    private val items = MutableStateFlow<List<RoutineTemplateItem>>(emptyList())
    val addedItems = mutableListOf<AddedTemplateItem>()

    override fun templateItems(): Flow<List<RoutineTemplateItem>> = items

    override suspend fun addTemplateItem(
        title: String,
        description: String?,
    ): Long {
        addedItems += AddedTemplateItem(title = title, description = description)
        return addedItems.size.toLong()
    }

    override suspend fun updateAction(
        actionId: Long,
        title: String,
        description: String?,
    ) = Unit

    override suspend fun removeTemplateItem(routineItemId: Long) = Unit

    override suspend fun reorderTemplateItems(routineItemIdsInOrder: List<Long>) = Unit
}

data class AddedTemplateItem(
    val title: String,
    val description: String?,
)
