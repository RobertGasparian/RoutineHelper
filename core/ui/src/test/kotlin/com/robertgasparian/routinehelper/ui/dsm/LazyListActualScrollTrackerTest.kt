package com.robertgasparian.routinehelper.ui.dsm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LazyListActualScrollTrackerTest {
    @Test
    fun `given scroll gesture without position change then list is not actually scrolling`() {
        val tracker = tracker()

        val isActuallyScrolling = tracker.update(
            snapshot(isScrollInProgress = true),
        )

        assertFalse(isActuallyScrolling)
    }

    @Test
    fun `given scroll gesture with offset change then list is actually scrolling`() {
        val tracker = tracker()

        val isActuallyScrolling = tracker.update(
            snapshot(
                isScrollInProgress = true,
                firstVisibleItemScrollOffset = 12,
            ),
        )

        assertTrue(isActuallyScrolling)
    }

    @Test
    fun `given active scroll after position change then stationary frame remains scrolling`() {
        val tracker = tracker()
        tracker.update(
            snapshot(
                isScrollInProgress = true,
                firstVisibleItemScrollOffset = 12,
            ),
        )

        val isActuallyScrolling = tracker.update(
            snapshot(
                isScrollInProgress = true,
                firstVisibleItemScrollOffset = 12,
            ),
        )

        assertTrue(isActuallyScrolling)
    }

    @Test
    fun `given actual scroll when gesture ends then list is no longer scrolling`() {
        val tracker = tracker()
        tracker.update(
            snapshot(
                isScrollInProgress = true,
                firstVisibleItemScrollOffset = 12,
            ),
        )

        val isActuallyScrolling = tracker.update(
            snapshot(
                isScrollInProgress = false,
                firstVisibleItemScrollOffset = 12,
            ),
        )

        assertFalse(isActuallyScrolling)
    }

    @Test
    fun `given position changes outside scroll then next stationary gesture stays visible`() {
        val tracker = tracker()
        tracker.update(
            snapshot(
                isScrollInProgress = false,
                firstVisibleItemIndex = 1,
            ),
        )

        val isActuallyScrolling = tracker.update(
            snapshot(
                isScrollInProgress = true,
                firstVisibleItemIndex = 1,
            ),
        )

        assertFalse(isActuallyScrolling)
    }

    private fun tracker(): LazyListActualScrollTracker = LazyListActualScrollTracker(
        firstVisibleItemIndex = 0,
        firstVisibleItemScrollOffset = 0,
    )

    private fun snapshot(
        isScrollInProgress: Boolean,
        firstVisibleItemIndex: Int = 0,
        firstVisibleItemScrollOffset: Int = 0,
    ): LazyListScrollSnapshot = LazyListScrollSnapshot(
        isScrollInProgress = isScrollInProgress,
        firstVisibleItemIndex = firstVisibleItemIndex,
        firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
    )
}
