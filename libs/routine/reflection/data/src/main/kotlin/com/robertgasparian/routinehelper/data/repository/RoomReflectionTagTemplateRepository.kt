package com.robertgasparian.routinehelper.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.data.local.dao.ReflectionTagDao
import com.robertgasparian.routinehelper.data.local.entity.ReflectionTagEntity
import com.robertgasparian.routinehelper.domain.model.ReflectionTagDefinition
import com.robertgasparian.routinehelper.domain.model.ReflectionTagInputNormalizer
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import com.robertgasparian.routinehelper.domain.repository.ReflectionTagTemplateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomReflectionTagTemplateRepository @Inject constructor(
    private val database: RoomDatabase,
    private val reflectionTagDao: ReflectionTagDao,
    private val inputNormalizer: ReflectionTagInputNormalizer,
    private val timeProvider: TimeProvider,
) : ReflectionTagTemplateRepository {
    override fun tags(cadence: RoutineCadence): Flow<List<ReflectionTagDefinition>> =
        reflectionTagDao.tags(cadence.toStorageValue()).map { tags ->
            tags.map(ReflectionTagEntity::toDomain)
        }

    override suspend fun reconcile(
        cadence: RoutineCadence,
        originalTagIds: Set<Long>,
        draft: List<ReflectionTagTemplateDraft>,
    ): List<SelectedReflectionTag> = database.withTransaction {
        val storageCadence = cadence.toStorageValue()
        val draftSourceIds = draft.mapNotNullTo(mutableSetOf(), ReflectionTagTemplateDraft::sourceTagId)
        (originalTagIds - draftSourceIds).forEach { tagId ->
            reflectionTagDao.delete(cadence = storageCadence, tagId = tagId)
        }

        val existingTagsById = reflectionTagDao.tagsOnce(storageCadence).associateBy(ReflectionTagEntity::id)
        draft.map { tag ->
            tag.sourceTagId?.let(existingTagsById::get)?.toDomain()
                ?: addTag(cadence = cadence, label = tag.label)
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

    private suspend fun addTag(
        cadence: RoutineCadence,
        label: String,
    ): ReflectionTagDefinition {
        val normalizedLabel = inputNormalizer.normalizeLabel(label)
        val normalizedKey = inputNormalizer.normalizedKey(normalizedLabel)
        val storageCadence = cadence.toStorageValue()
        return reflectionTagDao.tagByNormalizedLabel(storageCadence, normalizedKey)?.toDomain()
            ?: run {
                val now = timeProvider.currentTimeMillis()
                val entity = ReflectionTagEntity(
                    cadence = storageCadence,
                    label = normalizedLabel,
                    normalizedLabel = normalizedKey,
                    position = reflectionTagDao.nextPosition(storageCadence),
                    createdAtMillis = now,
                    updatedAtMillis = now,
                )
                entity.copy(id = reflectionTagDao.insert(entity)).toDomain()
            }
    }
}

internal fun RoutineCadence.toReflectionTagStorageValue(): String = toStorageValue()

private fun RoutineCadence.toStorageValue(): String =
    when (this) {
        RoutineCadence.Daily -> ReflectionTagEntity.DAILY_CADENCE_STORAGE_VALUE
        RoutineCadence.Weekly -> ReflectionTagEntity.WEEKLY_CADENCE_STORAGE_VALUE
    }

private fun String.toRoutineCadence(): RoutineCadence =
    when (this) {
        ReflectionTagEntity.DAILY_CADENCE_STORAGE_VALUE -> RoutineCadence.Daily
        ReflectionTagEntity.WEEKLY_CADENCE_STORAGE_VALUE -> RoutineCadence.Weekly
        else -> error("Unsupported reflection tag cadence storage value: $this")
    }

internal fun ReflectionTagEntity.toDomain(): ReflectionTagDefinition =
    ReflectionTagDefinition(
        id = id,
        label = label,
        position = position,
        cadence = cadence.toRoutineCadence(),
    )
