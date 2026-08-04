package com.robertgasparian.routinehelper.data.repository

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.core.testing.FixedTimeProvider
import com.robertgasparian.routinehelper.data.local.dao.ReflectionTagDao
import com.robertgasparian.routinehelper.data.local.entity.ReflectionTagEntity
import com.robertgasparian.routinehelper.domain.model.ReflectionTagInputNormalizer
import com.robertgasparian.routinehelper.domain.model.ReflectionTagTemplateDraft
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import java.util.concurrent.Executor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomReflectionTagTemplateRepositoryTest {
    private val database = ReflectionTestRoomDatabase()
    private val reflectionTagDao = FakeReflectionTagDao()
    private val timeProvider = FixedTimeProvider()
    private val repository = RoomReflectionTagTemplateRepository(
        database = database,
        reflectionTagDao = reflectionTagDao,
        inputNormalizer = ReflectionTagInputNormalizer(),
        timeProvider = timeProvider,
    )

    @Test
    fun `given an edited daily template when reconciled then mutation and selection are transactional`() = runTest {
        reflectionTagDao.seed(
            tag(id = 1L, label = "Calm", position = 0),
            tag(id = 2L, label = "Productive", position = 1),
            tag(id = 3L, label = "Added elsewhere", position = 2),
            tag(
                id = 4L,
                label = "Weekly only",
                position = 0,
                cadence = ReflectionTagEntity.WEEKLY_CADENCE_STORAGE_VALUE,
            ),
        )

        val selectedTags = repository.reconcile(
            cadence = RoutineCadence.Daily,
            originalTagIds = setOf(1L, 2L),
            draft = listOf(
                ReflectionTagTemplateDraft(sourceTagId = 1L, label = "Calm", isSelected = true),
                ReflectionTagTemplateDraft(
                    sourceTagId = null,
                    label = "  Focused   work  ",
                    isSelected = true,
                ),
            ),
        )

        assertEquals(
            listOf("Calm", "Added elsewhere", "Focused work"),
            repository.tags(RoutineCadence.Daily).first().map { definition -> definition.label },
        )
        assertEquals(
            listOf("Weekly only"),
            repository.tags(RoutineCadence.Weekly).first().map { definition -> definition.label },
        )
        assertEquals(
            listOf(
                SelectedReflectionTag(templateTagId = 1L, label = "Calm", position = 0),
                SelectedReflectionTag(templateTagId = 5L, label = "Focused work", position = 3),
            ),
            selectedTags,
        )
        assertEquals(
            timeProvider.currentTimeMillis(),
            reflectionTagDao.tagsById.getValue(5L).createdAtMillis,
        )
        assertEquals(1, database.transactionBegins)
        assertEquals(1, database.transactionSuccesses)
        assertEquals(1, database.transactionEnds)
    }

    @Test
    fun `given matching labels when new drafts are reconciled then cadence duplicate is reused`() = runTest {
        reflectionTagDao.seed(
            tag(id = 1L, label = "Calm", position = 0),
            tag(
                id = 2L,
                label = "Calm",
                position = 0,
                cadence = ReflectionTagEntity.WEEKLY_CADENCE_STORAGE_VALUE,
            ),
        )

        val selectedTags = repository.reconcile(
            cadence = RoutineCadence.Daily,
            originalTagIds = emptySet(),
            draft = listOf(
                ReflectionTagTemplateDraft(label = " calm ", isSelected = true),
                ReflectionTagTemplateDraft(label = "CALM", isSelected = true),
                ReflectionTagTemplateDraft(label = "New tag", isSelected = false),
            ),
        )

        assertEquals(
            listOf(SelectedReflectionTag(templateTagId = 1L, label = "Calm", position = 0)),
            selectedTags,
        )
        assertEquals(
            listOf("Calm", "New tag"),
            repository.tags(RoutineCadence.Daily).first().map { definition -> definition.label },
        )
        assertEquals(
            listOf("Calm"),
            repository.tags(RoutineCadence.Weekly).first().map { definition -> definition.label },
        )
    }

    private fun tag(
        id: Long,
        label: String,
        position: Int,
        cadence: String = ReflectionTagEntity.DAILY_CADENCE_STORAGE_VALUE,
    ): ReflectionTagEntity =
        ReflectionTagEntity(
            id = id,
            cadence = cadence,
            label = label,
            normalizedLabel = label.lowercase(),
            position = position,
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
        )
}

private class FakeReflectionTagDao : ReflectionTagDao {
    val tagsById = mutableMapOf<Long, ReflectionTagEntity>()
    private var nextId = 1L

    fun seed(vararg tags: ReflectionTagEntity) {
        tags.forEach { tag -> tagsById[tag.id] = tag }
        nextId = maxOf(nextId, tags.maxOfOrNull { tag -> tag.id + 1L } ?: nextId)
    }

    override fun tags(cadence: String): Flow<List<ReflectionTagEntity>> =
        flowOf(tagsForCadence(cadence))

    override suspend fun tagsOnce(cadence: String): List<ReflectionTagEntity> =
        tagsForCadence(cadence)

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
        tagsById.values.filter { tag -> tag.cadence == cadence }
            .maxOfOrNull(ReflectionTagEntity::position)
            ?.plus(1)
            ?: 0

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

    private fun tagsForCadence(cadence: String): List<ReflectionTagEntity> =
        tagsById.values.filter { tag -> tag.cadence == cadence }
            .sortedWith(compareBy(ReflectionTagEntity::position, ReflectionTagEntity::id))
}

@Suppress("OVERRIDE_DEPRECATION")
private class ReflectionTestRoomDatabase : RoomDatabase() {
    var transactionBegins = 0
        private set
    var transactionSuccesses = 0
        private set
    var transactionEnds = 0
        private set

    override val transactionExecutor: Executor = Executor(Runnable::run)

    override fun createInvalidationTracker(): InvalidationTracker =
        error("Invalidation tracking is not used by repository unit tests")

    override fun clearAllTables() = Unit

    override fun beginTransaction() {
        transactionBegins += 1
    }

    override fun setTransactionSuccessful() {
        transactionSuccesses += 1
    }

    override fun endTransaction() {
        transactionEnds += 1
    }
}
