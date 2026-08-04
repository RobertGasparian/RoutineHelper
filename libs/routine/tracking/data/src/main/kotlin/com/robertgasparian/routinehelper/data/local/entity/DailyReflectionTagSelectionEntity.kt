package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "daily_reflection_tag_selections",
    primaryKeys = ["date", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = DailyReflectionEntity::class,
            parentColumns = ["date"],
            childColumns = ["date"],
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
data class DailyReflectionTagSelectionEntity(
    val date: String,
    val tagId: Long,
)
