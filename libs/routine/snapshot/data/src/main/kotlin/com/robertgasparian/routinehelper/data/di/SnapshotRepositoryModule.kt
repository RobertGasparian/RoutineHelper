package com.robertgasparian.routinehelper.data.di

import com.robertgasparian.routinehelper.data.repository.RoomRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.repository.RoutineHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SnapshotRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRoutineHistoryRepository(
        repository: RoomRoutineHistoryRepository,
    ): RoutineHistoryRepository
}
