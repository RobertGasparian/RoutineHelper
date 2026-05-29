package com.robertgasparian.routinehelper.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntryEntity

data class DailySnapshotWithEntries(
    @Embedded
    val snapshot: DailySnapshotEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "snapshotId",
    )
    val entries: List<DailySnapshotEntryEntity>,
)
