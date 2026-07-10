package com.robertgasparian.routinehelper.ui.currentlist.undo

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Qualifier
annotation class CurrentListUndoScope

@Module
@InstallIn(SingletonComponent::class)
object CurrentListUndoCoroutineScopeModule {
    @Provides
    @Singleton
    @CurrentListUndoScope
    fun provideCurrentListUndoScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
