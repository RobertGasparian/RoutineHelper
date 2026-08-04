package com.robertgasparian.routinehelper.domain.usecase

import com.robertgasparian.routinehelper.domain.model.ReflectionTagDefinition
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.repository.FakeReflectionTagTemplateRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReconcileReflectionTagTemplateUseCaseTest {
    private val repository = FakeReflectionTagTemplateRepository()
    private val useCase = ReconcileReflectionTagTemplateUseCase(repository)

    @Test
    fun `given daily draft when reconciled then daily template changes and weekly template remains separate`() = runTest {
        repository.setTags(
            cadence = RoutineCadence.Daily,
            tags = listOf(
                tag(1L, "Calm", 0, RoutineCadence.Daily),
                tag(2L, "Productive", 1, RoutineCadence.Daily),
            ),
        )
        repository.setTags(
            cadence = RoutineCadence.Weekly,
            tags = listOf(tag(3L, "Calm", 0, RoutineCadence.Weekly)),
        )

        val selectedTags = useCase(
            cadence = RoutineCadence.Daily,
            originalTagIds = setOf(1L, 2L),
            draft = listOf(
                ReflectionTagTemplateDraft(sourceTagId = 1L, label = "Calm", isSelected = true),
                ReflectionTagTemplateDraft(label = "Focused", isSelected = true),
            ),
        )

        assertEquals(listOf("Calm", "Focused"), repository.tags(RoutineCadence.Daily).first().map { it.label })
        assertEquals(listOf("Calm"), repository.tags(RoutineCadence.Weekly).first().map { it.label })
        assertEquals(listOf("Calm", "Focused"), selectedTags.map { it.label })
    }

    private fun tag(
        id: Long,
        label: String,
        position: Int,
        cadence: RoutineCadence,
    ) = ReflectionTagDefinition(id = id, label = label, position = position, cadence = cadence)
}
