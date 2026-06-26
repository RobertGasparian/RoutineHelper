package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_snapshot_entries",
    foreignKeys = [
        ForeignKey(
            entity = RoutineSnapshotEntity::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["snapshotId"]),
        Index(value = ["actionId"]),
        Index(value = ["snapshotId", "positionSnapshot"]),
    ],
)
data class RoutineSnapshotEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val snapshotId: Long,
    val actionId: Long,
    val titleSnapshot: String,
    val descriptionSnapshot: String? = null,
    val positionSnapshot: Int,
    val isChecked: Boolean,
    val isHidden: Boolean = false,
    val repeatTargetCountSnapshot: Int? = null,
    val completedCount: Int = 0,
    val note: String? = null,
)
