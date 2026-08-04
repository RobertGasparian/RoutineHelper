package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "weekly_reflection_tag_selections",
    primaryKeys = ["weekStartDate", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = WeeklyReflectionEntity::class,
            parentColumns = ["weekStartDate"],
            childColumns = ["weekStartDate"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ReflectionTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["tagId"])],
)
data class WeeklyReflectionTagSelectionEntity(
    val weekStartDate: String,
    val tagId: Long,
)
