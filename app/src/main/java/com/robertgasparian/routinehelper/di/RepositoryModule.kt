package com.robertgasparian.routinehelper.di

import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import com.robertgasparian.routinehelper.data.repository.RoomRoutineHistoryRepository
import com.robertgasparian.routinehelper.data.repository.RoomRoutineTemplateRepository
import com.robertgasparian.routinehelper.data.repository.RoomTodayRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
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
    ): RoutineTemplateRepository = RoomRoutineTemplateRepository(database)

    @Provides
    @Singleton
    fun provideTodayRoutineRepository(
        database: RoutineDatabase,
    ): TodayRoutineRepository = RoomTodayRoutineRepository(database)

    @Provides
    @Singleton
    fun provideRoutineHistoryRepository(
        database: RoutineDatabase,
    ): RoutineHistoryRepository = RoomRoutineHistoryRepository(database)
}
