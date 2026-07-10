package com.robertgasparian.routinehelper.domain.order

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrentListOrderPlannerTest {
    private val planner = CurrentListOrderPlanner()

    @Test
    fun `given visible items when planning reorder then returns changed positions`() {
        val updates = planner.planReorder(
            allItems = listOf(
                orderItem(id = 10L, position = 0),
                orderItem(id = 11L, position = 1),
            ),
            visibleItemIdsInOrder = listOf(11L, 10L),
        )

        assertEquals(
            listOf(
                CurrentListPositionUpdate(itemId = 11L, position = 0),
                CurrentListPositionUpdate(itemId = 10L, position = 1),
            ),
            updates,
        )
    }

    @Test
    fun `given pending item when planning reorder then preserves hidden slot`() {
        val updates = planner.planReorder(
            allItems = listOf(
                orderItem(id = 10L, position = 0),
                orderItem(id = 11L, position = 1, isPendingRemoval = true),
                orderItem(id = 12L, position = 2),
                orderItem(id = 13L, position = 3),
            ),
            visibleItemIdsInOrder = listOf(13L, 10L, 12L),
        )

        assertEquals(
            listOf(
                CurrentListPositionUpdate(itemId = 13L, position = 0),
                CurrentListPositionUpdate(itemId = 10L, position = 2),
                CurrentListPositionUpdate(itemId = 12L, position = 3),
            ),
            updates,
        )
    }

    @Test
    fun `given unknown and omitted visible ids when planning reorder then ignores unknown ids and appends omitted items`() {
        val updates = planner.planReorder(
            allItems = listOf(
                orderItem(id = 10L, position = 0),
                orderItem(id = 11L, position = 1),
                orderItem(id = 12L, position = 2),
            ),
            visibleItemIdsInOrder = listOf(12L, 999L),
        )

        assertEquals(
            listOf(
                CurrentListPositionUpdate(itemId = 12L, position = 0),
                CurrentListPositionUpdate(itemId = 10L, position = 1),
                CurrentListPositionUpdate(itemId = 11L, position = 2),
            ),
            updates,
        )
    }

    private fun orderItem(
        id: Long,
        position: Int,
        isPendingRemoval: Boolean = false,
    ): CurrentListOrderItem =
        CurrentListOrderItem(
            id = id,
            position = position,
            isPendingRemoval = isPendingRemoval,
        )
}
