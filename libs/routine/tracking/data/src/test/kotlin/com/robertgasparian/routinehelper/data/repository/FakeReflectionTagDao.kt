package com.robertgasparian.routinehelper.data.repository

import com.robertgasparian.routinehelper.data.local.dao.ReflectionTagDao
import com.robertgasparian.routinehelper.data.local.entity.ReflectionTagEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeReflectionTagDao : ReflectionTagDao {
    val tagsById = mutableMapOf<Long, ReflectionTagEntity>()
    private var nextId = 1L

    override fun tags(cadence: String): Flow<List<ReflectionTagEntity>> =
        flowOf(
            tagsById.values.filter { tag -> tag.cadence == cadence }
                .sortedWith(compareBy(ReflectionTagEntity::position, ReflectionTagEntity::id)),
        )

    override suspend fun tagsOnce(cadence: String): List<ReflectionTagEntity> =
        tagsById.values.filter { tag -> tag.cadence == cadence }
            .sortedWith(compareBy(ReflectionTagEntity::position, ReflectionTagEntity::id))

    override suspend fun tagByNormalizedLabel(
        cadence: String,
        normalizedLabel: String,
    ): ReflectionTagEntity? =
        tagsById.values.firstOrNull { tag ->
            tag.cadence == cadence && tag.normalizedLabel == normalizedLabel
        }

    override suspend fun tagsByIds(
        cadence: String,
        tagIds: List<Long>,
    ): List<ReflectionTagEntity> =
        tagIds.mapNotNull(tagsById::get).filter { tag -> tag.cadence == cadence }

    override suspend fun nextPosition(cadence: String): Int =
        tagsById.values.filter { tag -> tag.cadence == cadence }.maxOfOrNull { tag -> tag.position }?.plus(1) ?: 0

    override suspend fun insert(tag: ReflectionTagEntity): Long {
        val id = tag.id.takeIf { it != 0L } ?: nextId++
        tagsById[id] = tag.copy(id = id)
        return id
    }

    override suspend fun delete(
        cadence: String,
        tagId: Long,
    ) {
        if (tagsById[tagId]?.cadence == cadence) tagsById.remove(tagId)
    }
}
