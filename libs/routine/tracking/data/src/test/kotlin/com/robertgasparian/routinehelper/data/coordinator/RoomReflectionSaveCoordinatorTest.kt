package com.robertgasparian.routinehelper.data.coordinator

import com.robertgasparian.routinehelper.data.repository.TrackingTestRoomDatabase
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.ReflectionTagDefinition
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import com.robertgasparian.routinehelper.domain.repository.FakeReflectionTagTemplateRepository
import com.robertgasparian.routinehelper.domain.repository.FakeTodayRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.FakeWeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.usecase.ReconcileReflectionTagTemplateUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateTodayReflectionUseCase
import com.robertgasparian.routinehelper.domain.usecase.UpdateWeeklyReflectionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomReflectionSaveCoordinatorTest {
    @Test
    fun `given daily reflection draft when saved then template and reflection share one transaction`() = runTest {
        val database = TrackingTestRoomDatabase()
        val tagRepository = FakeReflectionTagTemplateRepository().apply {
            setTags(
                cadence = RoutineCadence.Daily,
                tags = listOf(
                    ReflectionTagDefinition(7L, "Calm", 0, RoutineCadence.Daily),
                ),
            )
        }
        val todayRepository = FakeTodayRoutineRepository()
        val coordinator = RoomTodayReflectionSaveCoordinator(
            database = database,
            reconcileReflectionTagTemplateUseCase = ReconcileReflectionTagTemplateUseCase(tagRepository),
            updateTodayReflectionUseCase = UpdateTodayReflectionUseCase(todayRepository),
        )

        coordinator(
            date = "2026-08-04",
            reflection = RoutineReflection(summaryNote = "Steady", rating = ReflectionRating(4)),
            originalTagIds = setOf(7L),
            tagDraft = listOf(
                ReflectionTagTemplateDraft(sourceTagId = 7L, label = "Calm", isSelected = true),
            ),
        )

        assertEquals(
            RoutineReflection(
                summaryNote = "Steady",
                rating = ReflectionRating(4),
                selectedTags = listOf(
                    SelectedReflectionTag(templateTagId = 7L, label = "Calm", position = 0),
                ),
            ),
            todayRepository.reflectionChanges.single().reflection,
        )
        assertSingleSuccessfulTransaction(database)
    }

    @Test
    fun `given weekly reflection draft when saved then template and reflection share one transaction`() = runTest {
        val database = TrackingTestRoomDatabase()
        val tagRepository = FakeReflectionTagTemplateRepository().apply {
            setTags(
                cadence = RoutineCadence.Weekly,
                tags = listOf(
                    ReflectionTagDefinition(9L, "Balanced", 0, RoutineCadence.Weekly),
                ),
            )
        }
        val weeklyRepository = FakeWeeklyRoutineRepository()
        val coordinator = RoomWeeklyReflectionSaveCoordinator(
            database = database,
            reconcileReflectionTagTemplateUseCase = ReconcileReflectionTagTemplateUseCase(tagRepository),
            updateWeeklyReflectionUseCase = UpdateWeeklyReflectionUseCase(weeklyRepository),
        )

        coordinator(
            weekStartDate = "2026-08-03",
            reflection = RoutineReflection(summaryNote = "Good week", rating = ReflectionRating(5)),
            originalTagIds = setOf(9L),
            tagDraft = listOf(
                ReflectionTagTemplateDraft(sourceTagId = 9L, label = "Balanced", isSelected = true),
            ),
        )

        assertEquals(
            RoutineReflection(
                summaryNote = "Good week",
                rating = ReflectionRating(5),
                selectedTags = listOf(
                    SelectedReflectionTag(templateTagId = 9L, label = "Balanced", position = 0),
                ),
            ),
            weeklyRepository.reflectionChanges.single().reflection,
        )
        assertSingleSuccessfulTransaction(database)
    }

    private fun assertSingleSuccessfulTransaction(database: TrackingTestRoomDatabase) {
        assertEquals(1, database.transactionBegins)
        assertEquals(1, database.transactionSuccesses)
        assertEquals(1, database.transactionEnds)
    }
}
