package com.robertgasparian.routinehelper.domain.order

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineTemplateOrderPlannerTest {
    private val planner = RoutineTemplateOrderPlanner()

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
                RoutineTemplatePositionUpdate(itemId = 13L, position = 0),
                RoutineTemplatePositionUpdate(itemId = 10L, position = 2),
                RoutineTemplatePositionUpdate(itemId = 12L, position = 3),
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
                RoutineTemplatePositionUpdate(itemId = 12L, position = 0),
                RoutineTemplatePositionUpdate(itemId = 10L, position = 1),
                RoutineTemplatePositionUpdate(itemId = 11L, position = 2),
            ),
            updates,
        )
    }

    private fun orderItem(
        id: Long,
        position: Int,
        isPendingRemoval: Boolean = false,
    ): RoutineTemplateOrderItem =
        RoutineTemplateOrderItem(
            id = id,
            position = position,
            isPendingRemoval = isPendingRemoval,
        )
}
