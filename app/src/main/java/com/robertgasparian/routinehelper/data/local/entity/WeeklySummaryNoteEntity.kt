package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_summary_notes")
data class WeeklySummaryNoteEntity(
    @PrimaryKey
    val weekStartDate: String,
    val note: String,
    val updatedAtMillis: Long,
)
