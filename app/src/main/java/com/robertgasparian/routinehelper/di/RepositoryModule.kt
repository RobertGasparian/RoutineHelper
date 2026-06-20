package com.robertgasparian.routinehelper.di

import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import com.robertgasparian.routinehelper.data.repository.RoomRoutineHistoryRepository
import com.robertgasparian.routinehelper.data.repository.RoomRoutineTemplateRepository
import com.robertgasparian.routinehelper.data.repository.RoomTodayRoutineRepository
import com.robertgasparian.routinehelper.data.repository.RoomWeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideRoutineTemplateRepository(
        database: RoutineDatabase,
        timeProvider: TimeProvider,
    ): RoutineTemplateRepository =
        RoomRoutineTemplateRepository(
            database = database,
            actionDao = database.actionDao(),
            routineItemDao = database.routineItemDao(),
            timeProvider = timeProvider,
        )

    @Provides
    @Singleton
    fun provideTodayRoutineRepository(
        database: RoutineDatabase,
        timeProvider: TimeProvider,
    ): TodayRoutineRepository =
        RoomTodayRoutineRepository(
            routineItemDao = database.routineItemDao(),
            todayEntryDao = database.todayEntryDao(),
            dailySummaryNoteDao = database.dailySummaryNoteDao(),
            timeProvider = timeProvider,
        )

    @Provides
    @Singleton
    fun provideWeeklyRoutineRepository(
        database: RoutineDatabase,
        timeProvider: TimeProvider,
    ): WeeklyRoutineRepository =
        RoomWeeklyRoutineRepository(
            routineItemDao = database.routineItemDao(),
            weeklyEntryDao = database.weeklyEntryDao(),
            weeklySummaryNoteDao = database.weeklySummaryNoteDao(),
            timeProvider = timeProvider,
        )

    @Provides
    @Singleton
    fun provideRoutineHistoryRepository(
        database: RoutineDatabase,
    ): RoutineHistoryRepository =
        RoomRoutineHistoryRepository(
            database = database,
            dailySnapshotDao = database.dailySnapshotDao(),
        )
}
