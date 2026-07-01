package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_entries",
    foreignKeys = [
        ForeignKey(
            entity = RoutineItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["routineItemId"]),
        Index(value = ["date", "routineItemId"], unique = true),
    ],
)
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineItemId: Long,
    val date: String,
    val isChecked: Boolean = false,
    val completedCount: Int = 0,
    val isHidden: Boolean = false,
    val note: String? = null,
    val updatedAtMillis: Long,
)
