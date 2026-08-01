package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_reflections")
data class DailyReflectionEntity(
    @PrimaryKey
    val date: String,
    val summaryNote: String?,
    val rating: Int?,
    val updatedAtMillis: Long,
)
