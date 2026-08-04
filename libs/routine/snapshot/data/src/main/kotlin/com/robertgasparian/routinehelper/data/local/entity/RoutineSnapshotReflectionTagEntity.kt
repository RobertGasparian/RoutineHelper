package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "routine_snapshot_reflection_tags",
    primaryKeys = ["snapshotId", "normalizedLabelSnapshot"],
    foreignKeys = [
        ForeignKey(
            entity = RoutineSnapshotEntity::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["snapshotId", "positionSnapshot"]),
    ],
)
data class RoutineSnapshotReflectionTagEntity(
    val snapshotId: Long,
    val labelSnapshot: String,
    val normalizedLabelSnapshot: String,
    val positionSnapshot: Int,
)
