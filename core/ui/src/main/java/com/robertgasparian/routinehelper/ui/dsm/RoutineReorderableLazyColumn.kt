package com.robertgasparian.routinehelper.ui.dsm

import androidx.compose.animation.core.snap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class RoutineReorderDragStartMode {
    Immediate,
    LongPress,
}

@Stable
interface RoutineReorderableItemScope {
    /** Apply this modifier to the item UI that should initiate reordering. */
    val dragHandleModifier: Modifier

    /** True from pointer down on the handle until release or cancellation. */
    val isDragHandleActive: Boolean

    /** True after the drag gesture has started. */
    val isDragging: Boolean
}

/**
 * A reorderable lazy column that owns drag gestures, optimistic ordering, placement animation,
 * source reconciliation, and edge auto-scroll.
 *
 * [itemId] must return a unique, stable ID for each item. [onOrderChange] is invoked only when a
 * completed drag changes the order. The caller owns persistence and must eventually publish an
 * accepted order back through [items]; persistence rejection is not currently modeled by this API.
 * [header] is placed before the reorderable items and can optionally remain sticky, while [footer]
 * is placed after all reorderable items.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun <Item> RoutineReorderableLazyColumn(
    items: List<Item>,
    itemId: (Item) -> Long,
    onOrderChange: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    itemSpacing: Dp = 0.dp,
    dragStartMode: RoutineReorderDragStartMode = RoutineReorderDragStartMode.Immediate,
    header: (@Composable () -> Unit)? = null,
    stickyHeader: Boolean = false,
    footer: (@Composable () -> Unit)? = null,
    itemContent: @Composable RoutineReorderableItemScope.(Item) -> Unit,
) {
    val reorderState = remember { RoutineReorderState(initialItems = items) }
    val autoScroller = rememberRoutineReorderAutoScroller(listState = state)
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val reorderCoordinates = remember { RoutineReorderLayoutCoordinates() }
    val currentItemId = rememberUpdatedState(itemId)
    val currentOnOrderChange = rememberUpdatedState(onOrderChange)
    val idOf: (Item) -> Long = remember {
        { item -> currentItemId.value(item) }
    }
    val dispatchOrderChange: (List<Long>) -> Unit = remember {
        { orderedIds -> currentOnOrderChange.value(orderedIds) }
    }

    SideEffect {
        reorderState.syncFromSource(items = items, itemId = idOf)
    }
    RoutineReorderDropAnimation(
        reorderState = reorderState,
        itemId = idOf,
        onOrderChange = dispatchOrderChange,
    )

    val draggedItemId = reorderState.draggedItemId
    val isOverlayPositionAvailable = remember(reorderState) {
        derivedStateOf { reorderState.draggedItemTop != null }
    }
    val draggedItem = draggedItemId?.let { id ->
        reorderState.displayedItems.firstOrNull { item -> idOf(item) == id }
    }
    val isOverlayVisible = draggedItem != null && isOverlayPositionAvailable.value
    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            reorderCoordinates.containerTopInRoot = coordinates.positionInRoot().y
        },
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .reorderDragContainer(
                    reorderState = reorderState,
                    listState = state,
                    autoScroller = autoScroller,
                    layoutCoordinates = reorderCoordinates,
                    idOf = idOf,
                ),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            header?.let { content ->
                if (stickyHeader) {
                    stickyHeader(
                        key = ReorderHeaderKey,
                        contentType = ReorderHeaderContentType,
                    ) {
                        content()
                    }
                } else {
                    item(
                        key = ReorderHeaderKey,
                        contentType = ReorderHeaderContentType,
                    ) {
                        content()
                    }
                }
            }

            items(
                items = reorderState.displayedItems,
                key = idOf,
            ) { item ->
                val id = idOf(item)
                val isDragging = draggedItemId == id
                val dragHandleModifier = Modifier.reorderDragHandle(
                    itemId = id,
                    dragStartMode = dragStartMode,
                    reorderState = reorderState,
                )
                val itemScope = RoutineReorderableItemScopeImpl(
                    dragHandleModifier = dragHandleModifier,
                    isDragHandleActive = reorderState.pressedHandleItemId == id || isDragging,
                    isDragging = isDragging,
                )
                DisposableEffect(id) {
                    onDispose {
                        reorderCoordinates.onItemDisposed(itemId = id)
                    }
                }

                val placementModifier = if (isDragging) {
                    Modifier.animateItem(
                        fadeInSpec = null,
                        placementSpec = snap(),
                        fadeOutSpec = null,
                    )
                } else {
                    Modifier.animateItem()
                }
                Box(
                    modifier = placementModifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val itemBounds = reorderCoordinates.onItemPlaced(
                                itemId = id,
                                itemTopInRoot = coordinates.positionInRoot().y,
                                itemSize = coordinates.size.height,
                            )
                            if (isDragging) {
                                reorderState.onDraggedItemSlotPlaced(
                                    itemTop = itemBounds.top,
                                )
                            }
                        },
                ) {
                    if (isDragging && isOverlayVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(with(density) { reorderState.draggedItemSize.toDp() }),
                        )
                    } else {
                        itemContent(itemScope, item)
                    }
                }
            }

            footer?.let { content ->
                item(
                    key = ReorderFooterKey,
                    contentType = ReorderFooterContentType,
                ) {
                    content()
                }
            }
        }

        if (draggedItem != null && isOverlayPositionAvailable.value) {
            val overlayScope = RoutineReorderableItemScopeImpl(
                dragHandleModifier = Modifier,
                isDragHandleActive = true,
                isDragging = true,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .absolutePadding(
                        left = contentPadding.calculateLeftPadding(layoutDirection),
                        right = contentPadding.calculateRightPadding(layoutDirection),
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = 0,
                                y = reorderState.draggedItemTop?.roundToInt() ?: 0,
                            )
                        }
                        .fillMaxWidth(),
                ) {
                    itemContent(overlayScope, draggedItem)
                }
            }
        }
    }
}

private data class RoutineReorderableItemScopeImpl(
    override val dragHandleModifier: Modifier,
    override val isDragHandleActive: Boolean,
    override val isDragging: Boolean,
) : RoutineReorderableItemScope

internal data class RoutineReorderItemBounds(
    val top: Float,
    val size: Int,
)

internal class RoutineReorderLayoutCoordinates {
    var containerTopInRoot: Float = 0f

    private val itemBoundsById = mutableMapOf<Long, RoutineReorderItemBounds>()

    fun onItemPlaced(
        itemId: Long,
        itemTopInRoot: Float,
        itemSize: Int,
    ): RoutineReorderItemBounds = RoutineReorderItemBounds(
        top = itemTopInRoot - containerTopInRoot,
        size = itemSize,
    ).also { itemBounds ->
        itemBoundsById[itemId] = itemBounds
    }

    fun itemBounds(itemId: Long): RoutineReorderItemBounds? = itemBoundsById[itemId]

    fun onItemDisposed(itemId: Long) {
        itemBoundsById.remove(itemId)
    }
}

private const val ReorderHeaderKey = "routine-reorder-header"
private const val ReorderHeaderContentType = "routine-reorder-header"
private const val ReorderFooterKey = "routine-reorder-footer"
private const val ReorderFooterContentType = "routine-reorder-footer"
