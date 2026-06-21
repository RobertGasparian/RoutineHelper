package com.robertgasparian.routinehelper.ui.tracking

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteDateTimeTextProviderModule {
    @Binds
    abstract fun bindNoteDateTimeTextProvider(
        provider: AndroidNoteDateTimeTextProvider,
    ): NoteDateTimeTextProvider
}
