package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "daily_snapshots",
    indices = [
        Index(value = ["date", "cadence"], unique = true),
    ],
)
data class DailySnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val finalizedAtMillis: Long,
    @ColumnInfo(defaultValue = DAILY_CADENCE_STORAGE_VALUE)
    val cadence: String = DAILY_CADENCE_STORAGE_VALUE,
    val summaryNote: String? = null,
) {
    companion object {
        const val DAILY_CADENCE_STORAGE_VALUE = "DAILY"
        const val WEEKLY_CADENCE_STORAGE_VALUE = "WEEKLY"
    }
}
