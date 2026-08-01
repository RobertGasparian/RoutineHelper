package com.robertgasparian.routinehelper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.data.local.dao.ActionDao
import com.robertgasparian.routinehelper.data.local.dao.CurrentListItemDao
import com.robertgasparian.routinehelper.data.local.dao.DailyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.DailyReflectionDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineSnapshotDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyReflectionDao
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import com.robertgasparian.routinehelper.data.local.entity.CurrentListItemEntity
import com.robertgasparian.routinehelper.data.local.entity.DailyEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.DailyReflectionEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyReflectionEntity

@Database(
    entities = [
        ActionEntity::class,
        RoutineItemEntity::class,
        DailyEntryEntity::class,
        RoutineSnapshotEntity::class,
        RoutineSnapshotEntryEntity::class,
        WeeklyEntryEntity::class,
        DailyReflectionEntity::class,
        WeeklyReflectionEntity::class,
        CurrentListItemEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class RoutineDatabase : RoomDatabase() {
    abstract fun actionDao(): ActionDao

    abstract fun routineItemDao(): RoutineItemDao

    abstract fun dailyEntryDao(): DailyEntryDao

    abstract fun routineSnapshotDao(): RoutineSnapshotDao

    abstract fun weeklyEntryDao(): WeeklyEntryDao

    abstract fun dailyReflectionDao(): DailyReflectionDao

    abstract fun weeklyReflectionDao(): WeeklyReflectionDao

    abstract fun currentListItemDao(): CurrentListItemDao
}
