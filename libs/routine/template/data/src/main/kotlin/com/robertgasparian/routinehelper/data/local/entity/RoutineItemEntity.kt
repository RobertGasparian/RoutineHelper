package com.robertgasparian.routinehelper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

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
        Index(value = ["cadence", "position"]),
    ],
)
data class RoutineItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionId: Long,
    val position: Int,
    @ColumnInfo(defaultValue = DAILY_CADENCE_STORAGE_VALUE)
    val cadence: String = DAILY_CADENCE_STORAGE_VALUE,
    val createdAtMillis: Long,
) {
    companion object {
        const val DAILY_CADENCE_STORAGE_VALUE = "DAILY"
        const val WEEKLY_CADENCE_STORAGE_VALUE = "WEEKLY"
    }
}
