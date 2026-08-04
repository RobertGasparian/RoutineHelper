package com.robertgasparian.routinehelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.robertgasparian.routinehelper.data.local.entity.ReflectionTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReflectionTagDao {
    @Query(
        "SELECT * FROM reflection_tag_definitions " +
            "WHERE cadence = :cadence ORDER BY position, id",
    )
    fun tags(cadence: String): Flow<List<ReflectionTagEntity>>

    @Query(
        "SELECT * FROM reflection_tag_definitions " +
            "WHERE cadence = :cadence ORDER BY position, id",
    )
    suspend fun tagsOnce(cadence: String): List<ReflectionTagEntity>

    @Query(
        "SELECT * FROM reflection_tag_definitions " +
            "WHERE cadence = :cadence AND normalizedLabel = :normalizedLabel LIMIT 1",
    )
    suspend fun tagByNormalizedLabel(
        cadence: String,
        normalizedLabel: String,
    ): ReflectionTagEntity?

    @Query(
        "SELECT * FROM reflection_tag_definitions " +
            "WHERE cadence = :cadence AND id IN (:tagIds)",
    )
    suspend fun tagsByIds(
        cadence: String,
        tagIds: List<Long>,
    ): List<ReflectionTagEntity>

    @Query(
        "SELECT COALESCE(MAX(position), -1) + 1 FROM reflection_tag_definitions " +
            "WHERE cadence = :cadence",
    )
    suspend fun nextPosition(cadence: String): Int

    @Insert
    suspend fun insert(tag: ReflectionTagEntity): Long

    @Query("DELETE FROM reflection_tag_definitions WHERE id = :tagId AND cadence = :cadence")
    suspend fun delete(
        cadence: String,
        tagId: Long,
    )
}
