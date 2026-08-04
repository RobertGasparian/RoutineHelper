package com.robertgasparian.routinehelper.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.robertgasparian.routinehelper.data.local.Migration1To2
import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import com.robertgasparian.routinehelper.data.local.dao.ActionDao
import com.robertgasparian.routinehelper.data.local.dao.CurrentListItemDao
import com.robertgasparian.routinehelper.data.local.dao.DailyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.DailyReflectionDao
import com.robertgasparian.routinehelper.data.local.dao.ReflectionTagDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineSnapshotDao
import com.robertgasparian.routinehelper.data.local.dao.RoutineItemDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyEntryDao
import com.robertgasparian.routinehelper.data.local.dao.WeeklyReflectionDao
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
        )
            .addMigrations(Migration1To2)
            .build()

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
    fun provideDailyReflectionDao(database: RoutineDatabase): DailyReflectionDao = database.dailyReflectionDao()

    @Provides
    fun provideWeeklyReflectionDao(database: RoutineDatabase): WeeklyReflectionDao = database.weeklyReflectionDao()

    @Provides
    fun provideReflectionTagDao(database: RoutineDatabase): ReflectionTagDao = database.reflectionTagDao()

    @Provides
    fun provideRoutineSnapshotDao(database: RoutineDatabase): RoutineSnapshotDao = database.routineSnapshotDao()

    @Provides
    fun provideCurrentListItemDao(database: RoutineDatabase): CurrentListItemDao = database.currentListItemDao()
}
