package com.robertgasparian.routinehelper.di

import com.robertgasparian.routinehelper.core.time.SystemTimeProvider
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.ui.tracking.AndroidNoteDateTimeTextProvider
import com.robertgasparian.routinehelper.ui.tracking.NoteDateTimeTextProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider =
        SystemTimeProvider()

    @Provides
    fun provideNoteDateTimeTextProvider(
        provider: AndroidNoteDateTimeTextProvider,
    ): NoteDateTimeTextProvider =
        provider
}
