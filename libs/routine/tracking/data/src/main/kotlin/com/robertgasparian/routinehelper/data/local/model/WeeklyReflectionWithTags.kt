package com.robertgasparian.routinehelper.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.robertgasparian.routinehelper.data.local.entity.ReflectionTagEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyReflectionEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyReflectionTagSelectionEntity

data class WeeklyReflectionWithTags(
    @Embedded
    val reflection: WeeklyReflectionEntity,
    @Relation(
        parentColumn = "weekStartDate",
        entityColumn = "id",
        associateBy = Junction(
            value = WeeklyReflectionTagSelectionEntity::class,
            parentColumn = "weekStartDate",
            entityColumn = "tagId",
        ),
    )
    val tags: List<ReflectionTagEntity>,
)
