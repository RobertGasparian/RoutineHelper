package com.robertgasparian.routinehelper.core.time

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeProviderModule {
    @Binds
    @Singleton
    abstract fun bindTimeProvider(
        provider: SystemTimeProvider,
    ): TimeProvider
}
