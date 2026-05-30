package com.robertgasparian.routinehelper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.data.local.dao.ActionDao
import com.robertgasparian.routinehelper.data.local.dao.DailySnapshotDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.dao.TodayEntryDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyEntryDao
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.DailySnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.entity.TodayEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyEntryEntity

@Database(
    entities = [
        ActionEntity::class,
        RoutineItemEntity::class,
        TodayEntryEntity::class,
        DailySnapshotEntity::class,
        DailySnapshotEntryEntity::class,
        WeeklyEntryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class RoutineDatabase : RoomDatabase() {
    abstract fun actionDao(): ActionDao

    abstract fun routineItemDao(): RoutineItemDao

    abstract fun todayEntryDao(): TodayEntryDao

    abstract fun dailySnapshotDao(): DailySnapshotDao

    abstract fun weeklyEntryDao(): WeeklyEntryDao
}
