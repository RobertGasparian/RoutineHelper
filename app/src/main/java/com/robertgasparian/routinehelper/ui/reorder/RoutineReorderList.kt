package com.robertgasparian.routinehelper.ui.reorder

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.robertgasparian.routinehelper.ui.daily.DailyItemUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class RoutineReorderState {
    var displayedItems by mutableStateOf<List<DailyItemUiState>>(emptyList())
        private set
    var draggedItemId by mutableStateOf<Long?>(null)
        private set
    var draggedItemOffset by mutableFloatStateOf(0f)
        private set
    private var phase by mutableStateOf<RoutineReorderPhase>(RoutineReorderPhase.Idle)

    fun syncFromSource(items: List<DailyItemUiState>) {
        phase = when (val currentPhase = phase) {
            RoutineReorderPhase.Idle -> {
                displayedItems = items
                RoutineReorderPhase.Idle
            }
            is RoutineReorderPhase.Dragging -> {
                displayedItems = displayedItems.mergeItemContent(items)
                currentPhase
            }
            is RoutineReorderPhase.Saving -> {
                if (items.map(DailyItemUiState::routineItemId) == currentPhase.orderedIds) {
                    displayedItems = items
                    RoutineReorderPhase.Idle
                } else {
                    displayedItems = displayedItems.mergeItemContent(items)
                    currentPhase
                }
            }
        }
    }

    fun onDragStart(itemId: Long) {
        draggedItemId = itemId
        draggedItemOffset = 0f
        phase = RoutineReorderPhase.Dragging
    }

    fun onDrag(deltaY: Float) {
        draggedItemOffset += deltaY
    }

    fun move(fromIndex: Int, toIndex: Int, offsetAdjustment: Float) {
        displayedItems = displayedItems.moveItem(fromIndex, toIndex)
        draggedItemOffset += offsetAdjustment
    }

    fun onDragCancel(sourceItems: List<DailyItemUiState>) {
        draggedItemId = null
        draggedItemOffset = 0f
        displayedItems = sourceItems
        phase = RoutineReorderPhase.Idle
    }

    fun onDragEnd(sourceItems: List<DailyItemUiState>): List<Long>? {
        val orderedIds = displayedItems.map(DailyItemUiState::routineItemId)
        val sourceIds = sourceItems.map(DailyItemUiState::routineItemId)
        draggedItemId = null
        draggedItemOffset = 0f
        if (orderedIds == sourceIds) {
            displayedItems = sourceItems
            phase = RoutineReorderPhase.Idle
            return null
        }
        phase = RoutineReorderPhase.Saving(orderedIds)
        return orderedIds
    }
}

@Composable
fun rememberRoutineReorderState(): RoutineReorderState =
    remember { RoutineReorderState() }

@Composable
fun RoutineReorderList(
    sourceItems: List<DailyItemUiState>,
    reorderState: RoutineReorderState,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    contentPadding: PaddingValues,
    summaryContent: @Composable () -> Unit,
    onDropOrder: (List<Long>) -> Unit,
    itemContent: @Composable (
        item: DailyItemUiState,
        modifier: Modifier,
        dragHandleModifier: Modifier,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentSourceItems by rememberUpdatedState(sourceItems)

    LaunchedEffect(sourceItems) {
        reorderState.syncFromSource(sourceItems)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            summaryContent()
        }
        items(
            items = reorderState.displayedItems,
            key = { item -> item.routineItemId },
        ) { item ->
            val isDragging = reorderState.draggedItemId == item.routineItemId
            itemContent(
                item,
                Modifier
                    .animateItem()
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) reorderState.draggedItemOffset else 0f
                    },
                Modifier.pointerInput(item.routineItemId) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            reorderState.onDragStart(item.routineItemId)
                        },
                        onDragCancel = {
                            reorderState.onDragCancel(currentSourceItems)
                        },
                        onDragEnd = {
                            val orderedIds = reorderState.onDragEnd(currentSourceItems)
                            if (orderedIds != null) {
                                onDropOrder(orderedIds)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            reorderState.onDrag(dragAmount.y)
                            val move = calculateReorderMove(
                                items = reorderState.displayedItems,
                                itemInfos = listState.layoutInfo.visibleItemsInfo,
                                draggedItemId = item.routineItemId,
                                draggedItemOffset = reorderState.draggedItemOffset,
                            )
                            if (move != null) {
                                reorderState.move(move.fromIndex, move.toIndex, move.offsetAdjustment)
                            }
                            autoScrollIfNeeded(
                                listState = listState,
                                coroutineScope = coroutineScope,
                                draggedItemId = item.routineItemId,
                                draggedItemOffset = reorderState.draggedItemOffset,
                            )
                        },
                    )
                },
            )
        }
    }
}

private sealed interface RoutineReorderPhase {
    data object Idle : RoutineReorderPhase
    data object Dragging : RoutineReorderPhase

    data class Saving(
        val orderedIds: List<Long>,
    ) : RoutineReorderPhase
}

private data class ReorderMove(
    val fromIndex: Int,
    val toIndex: Int,
    val offsetAdjustment: Float,
)

private fun calculateReorderMove(
    items: List<DailyItemUiState>,
    itemInfos: List<LazyListItemInfo>,
    draggedItemId: Long,
    draggedItemOffset: Float,
): ReorderMove? {
    val fromIndex = items.indexOfFirst { item -> item.routineItemId == draggedItemId }
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
    val toIndex = items.indexOfFirst { item -> item.routineItemId == targetId }
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

private fun List<DailyItemUiState>.moveItem(
    fromIndex: Int,
    toIndex: Int,
): List<DailyItemUiState> =
    toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }

private fun List<DailyItemUiState>.mergeItemContent(sourceItems: List<DailyItemUiState>): List<DailyItemUiState> {
    val sourceById = sourceItems.associateBy(DailyItemUiState::routineItemId)
    val orderedIds = map(DailyItemUiState::routineItemId).toSet()
    val updatedOrderedItems = map { item -> sourceById[item.routineItemId] ?: item }
    val newItems = sourceItems.filterNot { item -> item.routineItemId in orderedIds }

    return updatedOrderedItems + newItems
}

private const val ReorderAutoScrollThresholdPx = 96
private const val ReorderAutoScrollStepPx = 36f
