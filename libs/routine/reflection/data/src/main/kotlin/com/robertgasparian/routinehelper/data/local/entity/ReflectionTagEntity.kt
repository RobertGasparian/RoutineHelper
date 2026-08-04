package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reflection_tag_definitions",
    indices = [
        Index(value = ["cadence", "normalizedLabel"], unique = true),
        Index(value = ["cadence", "position"]),
    ],
)
data class ReflectionTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cadence: String,
    val label: String,
    val normalizedLabel: String,
    val position: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    companion object {
        const val DAILY_CADENCE_STORAGE_VALUE = "DAILY"
        const val WEEKLY_CADENCE_STORAGE_VALUE = "WEEKLY"
    }
}
