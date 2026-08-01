package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_snapshots",
    indices = [
        Index(value = ["periodStartDate", "cadence"], unique = true),
    ],
)
data class RoutineSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periodStartDate: String,
    val finalizedAtMillis: Long,
    val cadence: String,
    val summaryNote: String? = null,
    val rating: Int? = null,
) {
    companion object {
        const val DAILY_CADENCE_STORAGE_VALUE = "DAILY"
        const val WEEKLY_CADENCE_STORAGE_VALUE = "WEEKLY"
    }
}
