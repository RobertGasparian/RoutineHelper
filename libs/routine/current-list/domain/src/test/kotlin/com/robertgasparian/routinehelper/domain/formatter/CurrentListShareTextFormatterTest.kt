package com.robertgasparian.routinehelper.domain.formatter

import com.robertgasparian.routinehelper.domain.model.CurrentListItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrentListShareTextFormatterTest {
    private val formatter = CurrentListShareTextFormatter()

    @Test
    fun `given current list items when formatting then emits ordered checklist text`() {
        val text = formatter(
            listOf(
                currentListItem(
                    id = 2L,
                    title = "Send invoice",
                    position = 1,
                    isChecked = true,
                ),
                currentListItem(
                    id = 1L,
                    title = "Pick up dry cleaning",
                    description = "Before 6 PM",
                    position = 0,
                ),
            ),
        )

        assertEquals(
            """
                Current list

                1. [ ] Pick up dry cleaning
                   Description: Before 6 PM

                2. [x] Send invoice
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun `given empty list when formatting then emits empty text`() {
        assertEquals(
            """
                Current list

                No items.
            """.trimIndent(),
            formatter(emptyList()),
        )
    }

    private fun currentListItem(
        id: Long,
        title: String,
        position: Int,
        description: String? = null,
        isChecked: Boolean = false,
    ): CurrentListItem =
        CurrentListItem(
            id = id,
            title = title,
            description = description,
            position = position,
            isChecked = isChecked,
        )
}
