package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_summary_notes")
data class DailySummaryNoteEntity(
    @PrimaryKey
    val date: String,
    val note: String,
    val updatedAtMillis: Long,
)
