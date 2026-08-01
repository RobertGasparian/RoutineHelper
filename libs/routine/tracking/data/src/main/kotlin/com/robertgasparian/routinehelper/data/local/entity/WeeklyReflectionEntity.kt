package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_reflections")
data class WeeklyReflectionEntity(
    @PrimaryKey
    val weekStartDate: String,
    val summaryNote: String?,
    val rating: Int?,
    val updatedAtMillis: Long,
)
