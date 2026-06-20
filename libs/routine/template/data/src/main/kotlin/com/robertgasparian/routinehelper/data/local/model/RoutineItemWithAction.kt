package com.robertgasparian.routinehelper.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity

data class RoutineItemWithAction(
    @Embedded
    val routineItem: RoutineItemEntity,
    @Relation(
        parentColumn = "actionId",
        entityColumn = "id",
    )
    val action: ActionEntity,
)
