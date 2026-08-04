package com.robertgasparian.routinehelper.data.di

import com.robertgasparian.routinehelper.data.coordinator.RoomTodayReflectionSaveCoordinator
import com.robertgasparian.routinehelper.data.coordinator.RoomWeeklyReflectionSaveCoordinator
import com.robertgasparian.routinehelper.data.repository.RoomTodayRoutineRepository
import com.robertgasparian.routinehelper.data.repository.RoomWeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.TodayRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.WeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.usecase.TodayReflectionSaveCoordinator
import com.robertgasparian.routinehelper.domain.usecase.WeeklyReflectionSaveCoordinator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingRepositoryModule {
    @Binds
    abstract fun bindTodayReflectionSaveCoordinator(
        coordinator: RoomTodayReflectionSaveCoordinator,
    ): TodayReflectionSaveCoordinator

    @Binds
    abstract fun bindWeeklyReflectionSaveCoordinator(
        coordinator: RoomWeeklyReflectionSaveCoordinator,
    ): WeeklyReflectionSaveCoordinator

    @Binds
    @Singleton
    abstract fun bindTodayRoutineRepository(
        repository: RoomTodayRoutineRepository,
    ): TodayRoutineRepository

    @Binds
    @Singleton
    abstract fun bindWeeklyRoutineRepository(
        repository: RoomWeeklyRoutineRepository,
    ): WeeklyRoutineRepository
}
