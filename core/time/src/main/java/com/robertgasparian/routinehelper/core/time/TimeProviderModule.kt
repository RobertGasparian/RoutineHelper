package com.robertgasparian.routinehelper.core.time

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeProviderModule {
    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider =
        SystemTimeProvider()
}
