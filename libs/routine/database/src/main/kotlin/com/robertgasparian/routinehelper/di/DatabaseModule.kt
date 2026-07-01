package com.robertgasparian.routinehelper.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import com.robertgasparian.routinehelper.data.local.dao.ActionDao
import com.robertgasparian.routinehelper.data.local.dao.DailyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.DailySummaryNoteDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineSnapshotDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklySummaryNoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideRoutineDatabase(
        @ApplicationContext context: Context,
    ): RoutineDatabase =
        Room.databaseBuilder(
            context,
            RoutineDatabase::class.java,
            "routine-helper.db",
        ).build()

    @Provides
    fun provideRoomDatabase(database: RoutineDatabase): RoomDatabase = database

    @Provides
    fun provideActionDao(database: RoutineDatabase): ActionDao = database.actionDao()

    @Provides
    fun provideRoutineItemDao(database: RoutineDatabase): RoutineItemDao = database.routineItemDao()

    @Provides
    fun provideDailyEntryDao(database: RoutineDatabase): DailyEntryDao = database.dailyEntryDao()

    @Provides
    fun provideWeeklyEntryDao(database: RoutineDatabase): WeeklyEntryDao = database.weeklyEntryDao()

    @Provides
    fun provideDailySummaryNoteDao(database: RoutineDatabase): DailySummaryNoteDao = database.dailySummaryNoteDao()

    @Provides
    fun provideWeeklySummaryNoteDao(database: RoutineDatabase): WeeklySummaryNoteDao = database.weeklySummaryNoteDao()

    @Provides
    fun provideRoutineSnapshotDao(database: RoutineDatabase): RoutineSnapshotDao = database.routineSnapshotDao()
}
