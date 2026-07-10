package com.robertgasparian.routinehelper.ui.currentlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class CurrentListReorderState(
    initialItems: List<CurrentListItemUiState> = emptyList(),
) {
    var displayedItems by mutableStateOf(initialItems)
        private set
    var pressedHandleItemId by mutableStateOf<Long?>(null)
        private set
    var draggedItemId by mutableStateOf<Long?>(null)
        private set
    var draggedItemOffset by mutableFloatStateOf(0f)
        private set
    private var sourceItems: List<CurrentListItemUiState> = initialItems
    private var phase by mutableStateOf<CurrentListReorderPhase>(CurrentListReorderPhase.Idle)

    fun syncFromSource(items: List<CurrentListItemUiState>) {
        sourceItems = items
        phase = when (val currentPhase = phase) {
            CurrentListReorderPhase.Idle -> {
                displayedItems = items
                CurrentListReorderPhase.Idle
            }
            is CurrentListReorderPhase.Dragging -> {
                displayedItems = displayedItems.mergeItemContent(items)
                currentPhase
            }
            is CurrentListReorderPhase.Saving -> {
                if (items.hasPersistedOrder(currentPhase.orderedIds)) {
                    displayedItems = items
                    CurrentListReorderPhase.Idle
                } else {
                    displayedItems = displayedItems.mergeItemContent(items)
                    currentPhase
                }
            }
        }
    }

    fun onDragStart(itemId: Long) {
        pressedHandleItemId = itemId
        draggedItemId = itemId
        draggedItemOffset = 0f
        phase = CurrentListReorderPhase.Dragging
    }

    fun onHandlePress(itemId: Long) {
        pressedHandleItemId = itemId
    }

    fun onHandleRelease(itemId: Long) {
        if (pressedHandleItemId == itemId) {
            pressedHandleItemId = null
        }
    }

    fun onDrag(deltaY: Float) {
        draggedItemOffset += deltaY
    }

    fun move(
        fromIndex: Int,
        toIndex: Int,
        offsetAdjustment: Float,
    ) {
        displayedItems = displayedItems.moveItem(fromIndex, toIndex)
        draggedItemOffset += offsetAdjustment
    }

    fun onDragCancel() {
        pressedHandleItemId = null
        if (draggedItemId == null) return

        draggedItemId = null
        draggedItemOffset = 0f
        displayedItems = sourceItems
        phase = CurrentListReorderPhase.Idle
    }

    fun onDragEnd(): List<Long>? {
        val orderedIds = displayedItems.map(CurrentListItemUiState::id)
        val sourceIds = sourceItems.map(CurrentListItemUiState::id)
        pressedHandleItemId = null
        draggedItemId = null
        draggedItemOffset = 0f
        if (orderedIds == sourceIds) {
            displayedItems = sourceItems
            phase = CurrentListReorderPhase.Idle
            return null
        }
        phase = CurrentListReorderPhase.Saving(orderedIds)
        return orderedIds
    }
}

@Composable
fun rememberCurrentListReorderState(
    initialItems: List<CurrentListItemUiState> = emptyList(),
): CurrentListReorderState =
    remember { CurrentListReorderState(initialItems) }

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CurrentListReorderList(
    sourceItems: List<CurrentListItemUiState>,
    reorderState: CurrentListReorderState,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    contentPadding: PaddingValues,
    onDropOrder: (List<Long>) -> Unit,
    stickyHeaderContent: (@Composable () -> Unit)? = null,
    itemContent: @Composable (
        item: CurrentListItemUiState,
        modifier: Modifier,
        dragHandleModifier: Modifier,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    SideEffect {
        reorderState.syncFromSource(sourceItems)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        stickyHeaderContent?.let { content ->
            stickyHeader(
                key = CurrentListBulkActionsHeaderKey,
                contentType = CurrentListBulkActionsHeaderContentType,
            ) {
                content()
            }
        }
        items(
            items = reorderState.displayedItems,
            key = { item -> item.id },
        ) { item ->
            val isDragging = reorderState.draggedItemId == item.id
            itemContent(
                item,
                Modifier
                    .animateItem()
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) reorderState.draggedItemOffset else 0f
                },
                Modifier
                    .pointerInput(item.id) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            reorderState.onHandlePress(item.id)
                            waitForUpOrCancellation()
                            reorderState.onHandleRelease(item.id)
                        }
                    }
                    .pointerInput(item.id) {
                        detectDragGestures(
                            orientationLock = Orientation.Vertical,
                            onDragStart = { _, _, _ ->
                                reorderState.onDragStart(item.id)
                            },
                            onDragCancel = {
                                reorderState.onDragCancel()
                            },
                            onDragEnd = {
                                val orderedIds = reorderState.onDragEnd()
                                if (orderedIds != null) {
                                    onDropOrder(orderedIds)
                                }
                            },
                            shouldAwaitTouchSlop = { true },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                reorderState.onDrag(dragAmount.y)
                                val move = calculateReorderMove(
                                    items = reorderState.displayedItems,
                                    itemInfos = listState.layoutInfo.visibleItemsInfo,
                                    draggedItemId = item.id,
                                    draggedItemOffset = reorderState.draggedItemOffset,
                                )
                                if (move != null) {
                                    reorderState.move(move.fromIndex, move.toIndex, move.offsetAdjustment)
                                }
                                autoScrollIfNeeded(
                                    listState = listState,
                                    coroutineScope = coroutineScope,
                                    draggedItemId = item.id,
                                    draggedItemOffset = reorderState.draggedItemOffset,
                                )
                            },
                        )
                    },
            )
        }
    }
}

private sealed interface CurrentListReorderPhase {
    data object Idle : CurrentListReorderPhase
    data object Dragging : CurrentListReorderPhase

    data class Saving(
        val orderedIds: List<Long>,
    ) : CurrentListReorderPhase
}

private data class ReorderMove(
    val fromIndex: Int,
    val toIndex: Int,
    val offsetAdjustment: Float,
)

private fun calculateReorderMove(
    items: List<CurrentListItemUiState>,
    itemInfos: List<LazyListItemInfo>,
    draggedItemId: Long,
    draggedItemOffset: Float,
): ReorderMove? {
    val fromIndex = items.indexOfFirst { item -> item.id == draggedItemId }
    if (fromIndex == -1) return null

    val draggedInfo = itemInfos.firstOrNull { itemInfo -> itemInfo.key == draggedItemId } ?: return null
    val draggedStart = draggedInfo.offset + draggedItemOffset
    val draggedEnd = draggedStart + draggedInfo.size

    val targetInfo = itemInfos
        .filter { itemInfo -> itemInfo.key is Long && itemInfo.key != draggedItemId }
        .firstOrNull { itemInfo ->
            itemInfo.offset > draggedInfo.offset && draggedEnd > itemInfo.offset + itemInfo.size / 2
        }
        ?: itemInfos
            .filter { itemInfo -> itemInfo.key is Long && itemInfo.key != draggedItemId }
            .lastOrNull { itemInfo ->
                itemInfo.offset < draggedInfo.offset && draggedStart < itemInfo.offset + itemInfo.size / 2
            }
        ?: return null

    val targetId = targetInfo.key as Long
    val toIndex = items.indexOfFirst { item -> item.id == targetId }
    if (toIndex == -1 || toIndex == fromIndex) return null

    return ReorderMove(
        fromIndex = fromIndex,
        toIndex = toIndex,
        offsetAdjustment = if (toIndex > fromIndex) {
            -targetInfo.size.toFloat()
        } else {
            targetInfo.size.toFloat()
        },
    )
}

private fun autoScrollIfNeeded(
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    draggedItemId: Long,
    draggedItemOffset: Float,
) {
    val draggedInfo = listState.layoutInfo.visibleItemsInfo
        .firstOrNull { itemInfo -> itemInfo.key == draggedItemId }
        ?: return
    val draggedStart = draggedInfo.offset + draggedItemOffset
    val draggedEnd = draggedStart + draggedInfo.size
    val viewportStart = listState.layoutInfo.viewportStartOffset
    val viewportEnd = listState.layoutInfo.viewportEndOffset
    when {
        draggedStart < viewportStart + ReorderAutoScrollThresholdPx -> {
            coroutineScope.launch { listState.scrollBy(-ReorderAutoScrollStepPx) }
        }
        draggedEnd > viewportEnd - ReorderAutoScrollThresholdPx -> {
            coroutineScope.launch { listState.scrollBy(ReorderAutoScrollStepPx) }
        }
    }
}

private fun List<CurrentListItemUiState>.moveItem(
    fromIndex: Int,
    toIndex: Int,
): List<CurrentListItemUiState> =
    toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }

private fun List<CurrentListItemUiState>.mergeItemContent(
    sourceItems: List<CurrentListItemUiState>,
): List<CurrentListItemUiState> {
    val sourceById = sourceItems.associateBy(CurrentListItemUiState::id)
    val orderedIds = map(CurrentListItemUiState::id).toSet()
    val updatedOrderedItems = mapNotNull { item -> sourceById[item.id] }
    val newItems = sourceItems.filterNot { item -> item.id in orderedIds }

    return updatedOrderedItems + newItems
}

private fun List<CurrentListItemUiState>.hasPersistedOrder(expectedIds: List<Long>): Boolean {
    val sourceIds = map(CurrentListItemUiState::id)
    val retainedExpectedIds = expectedIds.filter(sourceIds.toSet()::contains)
    return sourceIds.filter(retainedExpectedIds.toSet()::contains) == retainedExpectedIds
}

private const val ReorderAutoScrollThresholdPx = 96
private const val ReorderAutoScrollStepPx = 36f
private const val CurrentListBulkActionsHeaderKey = "current-list-bulk-actions-header"
private const val CurrentListBulkActionsHeaderContentType = "current-list-bulk-actions-header"
