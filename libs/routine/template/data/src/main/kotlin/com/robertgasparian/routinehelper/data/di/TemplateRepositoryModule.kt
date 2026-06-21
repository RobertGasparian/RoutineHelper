package com.robertgasparian.routinehelper.data.di

import com.robertgasparian.routinehelper.data.repository.RoomRoutineTemplateRepository
import com.robertgasparian.routinehelper.domain.repository.RoutineTemplateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TemplateRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRoutineTemplateRepository(
        repository: RoomRoutineTemplateRepository,
    ): RoutineTemplateRepository
}
