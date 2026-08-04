package com.robertgasparian.routinehelper.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.robertgasparian.routinehelper.data.local.dao.RoutineSnapshotDao
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotReflectionTagEntity
import com.robertgasparian.routinehelper.data.local.model.RoutineSnapshotWithEntries
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.ReflectionTagInputNormalizer
import com.robertgasparian.routinehelper.domain.model.RoutineReflection
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotItem
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRoutineHistoryRepository @Inject constructor(
    private val database: RoomDatabase,
    private val routineSnapshotDao: RoutineSnapshotDao,
    private val tagInputNormalizer: ReflectionTagInputNormalizer,
) : RoutineHistoryRepository {
    override fun snapshotSummaries(cadence: RoutineCadence?): Flow<List<RoutineSnapshotSummary>> =
        (cadence?.let { routineSnapshotDao.snapshotsWithEntries(it.toStorageValue()) }
            ?: routineSnapshotDao.snapshotsWithEntries()).map { snapshots ->
            snapshots.map { snapshotWithEntries ->
                snapshotWithEntries.toSummary()
            }
        }

    override fun snapshot(snapshotId: Long): Flow<RoutineSnapshot?> =
        routineSnapshotDao.snapshot(snapshotId).map { snapshotWithEntries ->
            snapshotWithEntries?.toDomain()
        }

    override suspend fun saveSnapshot(
        periodStartDate: String,
        finalizedAtMillis: Long,
        items: List<RoutineSnapshotItem>,
        reflection: RoutineReflection,
        cadence: RoutineCadence,
    ): Long = database.withTransaction {
        val storageCadence = cadence.toStorageValue()
        val normalizedReflection = normalizeReflection(reflection)
        val existingSnapshot = routineSnapshotDao.snapshotForPeriodStartDateOnce(periodStartDate, storageCadence)
        val snapshotId = existingSnapshot?.id ?: routineSnapshotDao.insertSnapshot(
            RoutineSnapshotEntity(
                periodStartDate = periodStartDate,
                finalizedAtMillis = finalizedAtMillis,
                cadence = storageCadence,
                summaryNote = normalizedReflection.summaryNote,
                rating = normalizedReflection.rating?.value,
            ),
        )
        if (existingSnapshot != null) {
            routineSnapshotDao.updateSnapshot(
                id = snapshotId,
                finalizedAtMillis = finalizedAtMillis,
                summaryNote = normalizedReflection.summaryNote,
                rating = normalizedReflection.rating?.value,
            )
            routineSnapshotDao.deleteEntries(snapshotId)
            routineSnapshotDao.deleteReflectionTags(snapshotId)
        }
        routineSnapshotDao.insertEntries(
            items.sortedBy { it.position }.map { item ->
                RoutineSnapshotEntryEntity(
                    snapshotId = snapshotId,
                    actionId = item.actionId,
                    titleSnapshot = item.title,
                    descriptionSnapshot = item.description,
                    positionSnapshot = item.position,
                    isChecked = item.isChecked,
                    isHidden = item.isHidden,
                    repeatTargetCountSnapshot = item.repeatTargetCount,
                    completedCount = item.completedCount,
                    note = item.note,
                )
            },
        )
        val reflectionTags = normalizedReflection.selectedTags.mapIndexed { position, tag ->
            RoutineSnapshotReflectionTagEntity(
                snapshotId = snapshotId,
                labelSnapshot = tag.label,
                normalizedLabelSnapshot = tagInputNormalizer.normalizedKey(tag.label),
                positionSnapshot = position,
            )
        }
        if (reflectionTags.isNotEmpty()) routineSnapshotDao.insertReflectionTags(reflectionTags)
        snapshotId
    }

    override suspend fun updateSnapshotReflection(
        snapshotId: Long,
        reflection: RoutineReflection,
    ) = database.withTransaction {
        val normalizedReflection = normalizeReflection(reflection)
        routineSnapshotDao.updateReflection(
            snapshotId = snapshotId,
            summaryNote = normalizedReflection.summaryNote,
            rating = normalizedReflection.rating?.value,
        )
        routineSnapshotDao.deleteReflectionTags(snapshotId)
        val tags = normalizedReflection.selectedTags.mapIndexed { position, tag ->
            RoutineSnapshotReflectionTagEntity(
                snapshotId = snapshotId,
                labelSnapshot = tag.label,
                normalizedLabelSnapshot = tagInputNormalizer.normalizedKey(tag.label),
                positionSnapshot = position,
            )
        }
        if (tags.isNotEmpty()) routineSnapshotDao.insertReflectionTags(tags)
    }

    override suspend fun deleteSnapshot(snapshotId: Long) {
        routineSnapshotDao.deleteSnapshot(snapshotId)
    }

    private fun normalizeReflection(reflection: RoutineReflection): RoutineReflection =
        reflection.copy(
            summaryNote = reflection.summaryNote?.trim()?.takeIf(String::isNotEmpty),
            selectedTags = reflection.selectedTags
                .map { tag -> tagInputNormalizer.normalizeLabel(tag.label) }
                .distinctBy(tagInputNormalizer::normalizedKey)
                .mapIndexed { position, label ->
                    SelectedReflectionTag(label = label, position = position)
                },
        )
}

private fun RoutineCadence.toStorageValue(): String =
    when (this) {
        RoutineCadence.Daily -> RoutineSnapshotEntity.DAILY_CADENCE_STORAGE_VALUE
        RoutineCadence.Weekly -> RoutineSnapshotEntity.WEEKLY_CADENCE_STORAGE_VALUE
    }

private fun String.toRoutineCadence(): RoutineCadence =
    when (this) {
        RoutineSnapshotEntity.DAILY_CADENCE_STORAGE_VALUE -> RoutineCadence.Daily
        RoutineSnapshotEntity.WEEKLY_CADENCE_STORAGE_VALUE -> RoutineCadence.Weekly
        else -> error("Unsupported routine cadence storage value: $this")
    }

private fun RoutineSnapshotWithEntries.toDomain(): RoutineSnapshot =
    RoutineSnapshot(
        snapshotId = snapshot.id,
        periodStartDate = snapshot.periodStartDate,
        finalizedAtMillis = snapshot.finalizedAtMillis,
        cadence = snapshot.cadence.toRoutineCadence(),
        summaryNote = snapshot.summaryNote,
        rating = snapshot.rating?.let(::ReflectionRating),
        selectedTags = reflectionTags
            .sortedBy(RoutineSnapshotReflectionTagEntity::positionSnapshot)
            .map { tag ->
                SelectedReflectionTag(
                    label = tag.labelSnapshot,
                    position = tag.positionSnapshot,
                )
            },
        items = entries.toDomainItems(),
        // Current policy: every stored snapshot summary is editable. Keep this explicit so a
        // future persisted or computed policy has a single mapping point.
        isReflectionEditable = true,
    )

private fun List<RoutineSnapshotEntryEntity>.toDomainItems(): List<RoutineSnapshotItem> =
    sortedBy { it.positionSnapshot }.map { entry ->
        RoutineSnapshotItem(
            actionId = entry.actionId,
            title = entry.titleSnapshot,
            description = entry.descriptionSnapshot,
            repeatTargetCount = entry.repeatTargetCountSnapshot,
            completedCount = entry.completedCount,
            position = entry.positionSnapshot,
            isChecked = entry.isChecked,
            isHidden = entry.isHidden,
            note = entry.note,
        )
    }

private fun RoutineSnapshotWithEntries.toSummary(): RoutineSnapshotSummary {
    val countableEntries = entries.filterNot { entry -> entry.isHidden }
    return RoutineSnapshotSummary(
        snapshotId = snapshot.id,
        periodStartDate = snapshot.periodStartDate,
        finalizedAtMillis = snapshot.finalizedAtMillis,
        cadence = snapshot.cadence.toRoutineCadence(),
        completedCount = countableEntries.count { entry ->
            entry.repeatTargetCountSnapshot?.let { target ->
                entry.completedCount >= target
            } ?: entry.isChecked
        },
        totalCount = countableEntries.size,
        hasSummaryNote = !snapshot.summaryNote.isNullOrBlank(),
    )
}
