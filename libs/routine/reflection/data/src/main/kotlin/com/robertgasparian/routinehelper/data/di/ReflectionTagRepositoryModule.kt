package com.robertgasparian.routinehelper.data.di

import com.robertgasparian.routinehelper.data.repository.RoomReflectionTagTemplateRepository
import com.robertgasparian.routinehelper.domain.repository.ReflectionTagTemplateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReflectionTagRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindReflectionTagTemplateRepository(
        implementation: RoomReflectionTagTemplateRepository,
    ): ReflectionTagTemplateRepository
}
