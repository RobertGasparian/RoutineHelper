package com.robertgasparian.routinehelper.data.di

import com.robertgasparian.routinehelper.data.repository.RoomCurrentListRepository
import com.robertgasparian.routinehelper.domain.repository.CurrentListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CurrentListRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCurrentListRepository(
        repository: RoomCurrentListRepository,
    ): CurrentListRepository
}
