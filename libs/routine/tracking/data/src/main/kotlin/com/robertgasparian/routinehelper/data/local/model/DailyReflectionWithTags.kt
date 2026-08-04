package com.robertgasparian.routinehelper.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.robertgasparian.routinehelper.data.local.entity.DailyReflectionEntity
import com.robertgasparian.routinehelper.data.local.entity.DailyReflectionTagSelectionEntity
import com.robertgasparian.routinehelper.data.local.entity.ReflectionTagEntity

data class DailyReflectionWithTags(
    @Embedded
    val reflection: DailyReflectionEntity,
    @Relation(
        parentColumn = "date",
        entityColumn = "id",
        associateBy = Junction(
            value = DailyReflectionTagSelectionEntity::class,
            parentColumn = "date",
            entityColumn = "tagId",
        ),
    )
    val tags: List<ReflectionTagEntity>,
)
