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
    @ColumnInfo(defaultValue = "DAILY")
    val cadence: String = "DAILY",
    val summaryNote: String? = null,
)
