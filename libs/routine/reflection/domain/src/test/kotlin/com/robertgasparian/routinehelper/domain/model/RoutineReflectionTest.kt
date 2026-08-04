package com.robertgasparian.routinehelper.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineReflectionTest {
    @Test
    fun `given no text or rating when checking emptiness then reflection is empty`() {
        assertTrue(RoutineReflection().isEmpty)
        assertTrue(RoutineReflection(summaryNote = "   ").isEmpty)
    }

    @Test
    fun `given only a rating when checking emptiness then reflection is not empty`() {
        assertFalse(RoutineReflection(rating = ReflectionRating(3)).isEmpty)
    }

    @Test
    fun `given only a selected tag when checking emptiness then reflection is not empty`() {
        assertFalse(
            RoutineReflection(
                selectedTags = listOf(SelectedReflectionTag(label = "Calm", position = 0)),
            ).isEmpty,
        )
    }

    @Test
    fun `given rating at supported bounds when creating rating then creation succeeds`() {
        ReflectionRating(ReflectionRating.MINIMUM)
        ReflectionRating(ReflectionRating.MAXIMUM)
    }

    @Test
    fun `given rating outside supported range when creating rating then creation fails`() {
        assertThrows(IllegalArgumentException::class.java) {
            ReflectionRating(ReflectionRating.MAXIMUM + 1)
        }
    }
}
