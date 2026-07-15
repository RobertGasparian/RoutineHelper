package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.foundation.lazy.LazyListItemInfo

internal data class ReorderMove(
    val fromIndex: Int,
    val toIndex: Int,
)

internal enum class ReorderDirection {
    Up,
    Down,
}

internal data class ReorderItemLayout(
    val id: Long,
    val offset: Int,
    val size: Int,
)

internal fun <Item> calculateReorderMove(
    items: List<Item>,
    itemInfos: List<LazyListItemInfo>,
    draggedItemId: Long,
    draggedItemTop: Float,
    draggedItemSize: Int,
    direction: ReorderDirection,
    itemId: (Item) -> Long,
): ReorderMove? {
    val itemIds = items.map(itemId)
    val itemIdSet = itemIds.toSet()
    val itemLayouts = itemInfos.mapNotNull { itemInfo ->
        val id = itemInfo.key as? Long
        if (id == null || id !in itemIdSet) {
            null
        } else {
            ReorderItemLayout(
                id = id,
                offset = itemInfo.offset,
                size = itemInfo.size,
            )
        }
    }
    return calculateReorderMove(
        itemIds = itemIds,
        itemLayouts = itemLayouts,
        draggedItemId = draggedItemId,
        draggedItemTop = draggedItemTop,
        draggedItemSize = draggedItemSize,
        direction = direction,
    )
}

internal fun calculateReorderMove(
    itemIds: List<Long>,
    itemLayouts: List<ReorderItemLayout>,
    draggedItemId: Long,
    draggedItemTop: Float,
    draggedItemSize: Int,
    direction: ReorderDirection,
): ReorderMove? {
    val fromIndex = itemIds.indexOf(draggedItemId)
    if (fromIndex == -1) return null

    val targetIndex = when (direction) {
        ReorderDirection.Up -> fromIndex - 1
        ReorderDirection.Down -> fromIndex + 1
    }
    val targetId = itemIds.getOrNull(targetIndex) ?: return null
    val targetLayout = itemLayouts.firstOrNull { layout -> layout.id == targetId } ?: return null
    val draggedCenter = draggedItemTop + draggedItemSize / 2f
    val hasCrossedTarget = when (direction) {
        ReorderDirection.Up -> draggedCenter < targetLayout.center
        ReorderDirection.Down -> draggedCenter > targetLayout.center
    }
    if (!hasCrossedTarget) return null

    return ReorderMove(
        fromIndex = fromIndex,
        toIndex = targetIndex,
    )
}

private val ReorderItemLayout.center: Float
    get() = offset + size / 2f
