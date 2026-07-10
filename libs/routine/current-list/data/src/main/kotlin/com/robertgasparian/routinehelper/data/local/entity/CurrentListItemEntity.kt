package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "current_list_items",
    indices = [
        Index(value = ["position"]),
    ],
)
data class CurrentListItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val position: Int,
    val isChecked: Boolean,
    val pendingRemovalAtMillis: Long? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
