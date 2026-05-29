package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_items",
    foreignKeys = [
        ForeignKey(
            entity = ActionEntity::class,
            parentColumns = ["id"],
            childColumns = ["actionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["actionId"], unique = true),
        Index(value = ["position"]),
    ],
)
data class RoutineItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionId: Long,
    val position: Int,
    val createdAtMillis: Long,
)
