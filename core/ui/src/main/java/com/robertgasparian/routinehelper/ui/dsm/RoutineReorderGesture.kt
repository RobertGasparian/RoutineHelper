package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput

internal fun <Item> Modifier.reorderDragHandle(
    itemId: Long,
    dragStartMode: RoutineReorderDragStartMode,
    reorderState: RoutineReorderState<Item>,
): Modifier =
    pointerInput(itemId) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            reorderState.onHandlePress(itemId)
            waitForUpOrCancellation()
            reorderState.onHandleRelease(itemId)
        }
    }.pointerInput(itemId, dragStartMode) {
        val onDragStart = {
            reorderState.onDragStart(itemId)
        }
        val onDragCancel = {
            if (reorderState.draggedItemId != itemId) {
                reorderState.onHandleRelease(itemId)
            }
        }
        val consumeDrag: (PointerInputChange, Offset) -> Unit = { change, dragAmount ->
            if (reorderState.draggedItemId == itemId && !reorderState.isContainerDragActive) {
                reorderState.onDrag(dragAmount.y)
            }
            change.consume()
        }

        when (dragStartMode) {
            RoutineReorderDragStartMode.Immediate -> detectDragGestures(
                orientationLock = Orientation.Vertical,
                onDragStart = { _, _, _ -> onDragStart() },
                onDragCancel = onDragCancel,
                onDragEnd = {},
                shouldAwaitTouchSlop = { true },
                onDrag = consumeDrag,
            )
            RoutineReorderDragStartMode.LongPress -> detectDragGesturesAfterLongPress(
                onDragStart = { onDragStart() },
                onDragCancel = onDragCancel,
                onDragEnd = {},
                onDrag = consumeDrag,
            )
        }
    }

internal fun <Item> Modifier.reorderDragContainer(
    reorderState: RoutineReorderState<Item>,
    listState: LazyListState,
    autoScroller: RoutineReorderAutoScroller,
    idOf: (Item) -> Long,
): Modifier =
    pointerInput(reorderState, listState, autoScroller) {
        fun applyReorderMove(
            draggedItemId: Long,
            direction: ReorderDirection,
        ) {
            val draggedItemTop = reorderState.draggedItemTop ?: return
            val move = calculateReorderMove(
                items = reorderState.displayedItems,
                itemInfos = listState.layoutInfo.visibleItemsInfo,
                draggedItemId = draggedItemId,
                draggedItemTop = draggedItemTop,
                draggedItemSize = reorderState.draggedItemSize,
                direction = direction,
                itemId = idOf,
            ) ?: return
            reorderState.move(
                fromIndex = move.fromIndex,
                toIndex = move.toIndex,
            )
        }

        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Initial,
            )
            var activeDraggedItemId: Long? = null

            try {
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { pointerChange ->
                        pointerChange.id == down.id
                    } ?: break
                    val draggedItemId = reorderState.draggedItemId

                    if (draggedItemId != null && !reorderState.isDropAnimating) {
                        if (activeDraggedItemId == null) {
                            val draggedInfo = listState.layoutInfo.visibleItemsInfo
                                .firstOrNull { itemInfo -> itemInfo.key == draggedItemId }
                            if (draggedInfo == null) {
                                reorderState.onDragCancel()
                                break
                            }
                            activeDraggedItemId = draggedItemId
                            reorderState.onContainerDragStart(
                                itemTop = draggedInfo.offset.toFloat(),
                                itemSize = draggedInfo.size,
                            )
                            autoScroller.start(
                                draggedItemTop = { reorderState.draggedItemTop },
                                draggedItemSize = { reorderState.draggedItemSize },
                                onScroll = { consumedScroll ->
                                    applyReorderMove(
                                        draggedItemId = draggedItemId,
                                        direction = consumedScroll.toReorderDirection(),
                                    )
                                },
                            )
                        }

                        if (activeDraggedItemId == draggedItemId) {
                            val dragDeltaY = change.position.y - change.previousPosition.y
                            change.consume()
                            if (change.pressed && dragDeltaY != 0f) {
                                reorderState.onDrag(dragDeltaY)
                                applyReorderMove(
                                    draggedItemId = draggedItemId,
                                    direction = dragDeltaY.toReorderDirection(),
                                )
                            }
                        }
                    }

                    if (change.changedToUpIgnoreConsumed()) {
                        if (activeDraggedItemId != null) {
                            autoScroller.stop()
                            reorderState.onDropAnimationStart()
                        }
                        break
                    }
                }
            } finally {
                if (
                    activeDraggedItemId != null &&
                    reorderState.draggedItemId != null &&
                    !reorderState.isDropAnimating
                ) {
                    autoScroller.stop()
                    reorderState.onDragCancel()
                }
            }
        }
    }

private fun Float.toReorderDirection(): ReorderDirection =
    if (this < 0f) ReorderDirection.Up else ReorderDirection.Down
