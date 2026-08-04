package com.robertgasparian.routinehelper.domain.repository

import com.robertgasparian.routinehelper.domain.model.ReflectionTagDefinition
import com.robertgasparian.routinehelper.domain.model.ReflectionTagInputNormalizer
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeReflectionTagTemplateRepository : ReflectionTagTemplateRepository {
    private val normalizer = ReflectionTagInputNormalizer()
    private val tagsByCadence = RoutineCadence.entries.associateWith {
        MutableStateFlow(emptyList<ReflectionTagDefinition>())
    }
    private var nextId = 1L

    override fun tags(cadence: RoutineCadence): Flow<List<ReflectionTagDefinition>> =
        tagsByCadence.getValue(cadence)

    override suspend fun reconcile(
        cadence: RoutineCadence,
        originalTagIds: Set<Long>,
        draft: List<ReflectionTagTemplateDraft>,
    ): List<SelectedReflectionTag> {
        val draftSourceIds = draft.mapNotNullTo(mutableSetOf(), ReflectionTagTemplateDraft::sourceTagId)
        val cadenceTags = tagsByCadence.getValue(cadence)
        cadenceTags.value = cadenceTags.value.filterNot { tag ->
            tag.id in originalTagIds && tag.id !in draftSourceIds
        }
        val existingTagsById = cadenceTags.value.associateBy(ReflectionTagDefinition::id)
        return draft.map { tag ->
            tag.sourceTagId?.let(existingTagsById::get) ?: addTag(cadence, tag.label)
        }
            .zip(draft)
            .filter { (_, draftTag) -> draftTag.isSelected }
            .distinctBy { (definition, _) -> definition.id }
            .map { (definition, _) ->
                SelectedReflectionTag(
                    templateTagId = definition.id,
                    label = definition.label,
                    position = definition.position,
                )
            }
    }

    private fun addTag(
        cadence: RoutineCadence,
        label: String,
    ): ReflectionTagDefinition {
        val normalizedLabel = normalizer.normalizeLabel(label)
        val currentTags = tagsByCadence.getValue(cadence).value
        currentTags.firstOrNull { tag ->
            normalizer.normalizedKey(tag.label) == normalizer.normalizedKey(normalizedLabel)
        }?.let { return it }

        val tag = ReflectionTagDefinition(
            id = nextId++,
            label = normalizedLabel,
            position = currentTags.size,
            cadence = cadence,
        )
        tagsByCadence.getValue(cadence).value = currentTags + tag
        return tag
    }

    fun setTags(
        cadence: RoutineCadence,
        tags: List<ReflectionTagDefinition>,
    ) {
        require(tags.all { tag -> tag.cadence == cadence })
        tagsByCadence.getValue(cadence).value = tags
        nextId = maxOf(nextId, tags.maxOfOrNull { tag -> tag.id + 1L } ?: nextId)
    }
}
