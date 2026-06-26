package com.robertgasparian.routinehelper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.data.local.dao.ActionDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineSnapshotDao
import com.robertgasparian.routinehelper.data.local.dao.DailySummaryNoteDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.dao.TodayEntryDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklySummaryNoteDao
import com.robertgasparian.routinehelper.data.local.entity.ActionEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineSnapshotEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.DailySummaryNoteEntity
import com.robertgasparian.routinehelper.data.local.entity.RoutineItemEntity
import com.robertgasparian.routinehelper.data.local.entity.TodayEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklyEntryEntity
import com.robertgasparian.routinehelper.data.local.entity.WeeklySummaryNoteEntity

@Database(
    entities = [
        ActionEntity::class,
        RoutineItemEntity::class,
        TodayEntryEntity::class,
        RoutineSnapshotEntity::class,
        RoutineSnapshotEntryEntity::class,
        WeeklyEntryEntity::class,
        DailySummaryNoteEntity::class,
        WeeklySummaryNoteEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class RoutineDatabase : RoomDatabase() {
    abstract fun actionDao(): ActionDao

    abstract fun routineItemDao(): RoutineItemDao

    abstract fun todayEntryDao(): TodayEntryDao

    abstract fun routineSnapshotDao(): RoutineSnapshotDao

    abstract fun weeklyEntryDao(): WeeklyEntryDao

    abstract fun dailySummaryNoteDao(): DailySummaryNoteDao

    abstract fun weeklySummaryNoteDao(): WeeklySummaryNoteDao
}
