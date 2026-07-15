package com.robertgasparian.routinehelper.ui.dsm

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineReorderDropAnimationTest {
    @Test
    fun `given different distances then duration preserves constant speed`() {
        assertEquals(
            100,
            calculateRoutineReorderDropDurationMillis(
                initialTop = 0f,
                targetTop = 100f,
                speedPxPerSecond = 1_000f,
            ),
        )
        assertEquals(
            250,
            calculateRoutineReorderDropDurationMillis(
                initialTop = 0f,
                targetTop = 250f,
                speedPxPerSecond = 1_000f,
            ),
        )
    }

    @Test
    fun `given no distance then duration is zero`() {
        assertEquals(
            0,
            calculateRoutineReorderDropDurationMillis(
                initialTop = 100f,
                targetTop = 100f,
                speedPxPerSecond = 1_000f,
            ),
        )
    }
}
