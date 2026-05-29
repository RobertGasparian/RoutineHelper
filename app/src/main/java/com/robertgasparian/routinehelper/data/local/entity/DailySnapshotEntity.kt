package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_snapshots",
    indices = [
        Index(value = ["date"], unique = true),
    ],
)
data class DailySnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val finalizedAtMillis: Long,
)
