package com.robertgasparian.routinehelper.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntryEntity

data class RoutineSnapshotWithEntries(
    @Embedded
    val snapshot: RoutineSnapshotEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "snapshotId",
    )
    val entries: List<RoutineSnapshotEntryEntity>,
)
