package com.robertgasparian.routinehelper.ui.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelBackStackTest {
    @Test
    fun `given start destination when creating back stack then exposes it as the only top-level entry`() {
        val backStack = TopLevelBackStack(startKey = "daily")

        assertEquals("daily", backStack.topLevelKey)
        assertEquals(listOf("daily"), backStack.backStack)
    }

    @Test
    fun `given nested destinations when removing last then removes only the latest entry`() {
        val backStack = TopLevelBackStack(startKey = "daily")
        backStack.add("editor")
        backStack.add("details")

        val handled = backStack.removeLast()

        assertTrue(handled)
        assertEquals(listOf("daily", "editor"), backStack.backStack)
        assertEquals("daily", backStack.topLevelKey)
    }

    @Test
    fun `given only root destination when removing last then keeps root and reports not handled`() {
        val backStack = TopLevelBackStack(startKey = "daily")

        val handled = backStack.removeLast()

        assertFalse(handled)
        assertEquals(listOf("daily"), backStack.backStack)
    }

    @Test
    fun `given nested stack when selecting another top level then clears history and updates top level`() {
        val backStack = TopLevelBackStack(startKey = "daily")
        backStack.add("editor")

        backStack.addTopLevel("weekly")

        assertEquals("weekly", backStack.topLevelKey)
        assertEquals(listOf("weekly"), backStack.backStack)
    }

    @Test
    fun `given top level path when replacing stack then root and nested destinations match manual navigation`() {
        val backStack = TopLevelBackStack(startKey = "daily").apply {
            add("editor")
        }

        backStack.replaceWithTopLevelPath(
            topLevelKey = "history",
            nestedKeys = listOf("snapshot"),
        )

        assertEquals("history", backStack.topLevelKey)
        assertEquals(listOf("history", "snapshot"), backStack.backStack)
        assertTrue(backStack.removeLast())
        assertEquals(listOf("history"), backStack.backStack)
        assertFalse(backStack.removeLast())
    }
}
