package com.robertgasparian.routinehelper.ui.removalundo

import com.robertgasparian.routinehelper.domain.removal.DefaultRoutineRemovalUndoCoordinator
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalUndoCoordinator
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalUndoScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutineRemovalUndoBindingModule {
    @Binds
    @Singleton
    abstract fun bindRoutineRemovalUndoCoordinator(
        implementation: DefaultRoutineRemovalUndoCoordinator,
    ): RoutineRemovalUndoCoordinator
}

@Module
@InstallIn(SingletonComponent::class)
object RoutineRemovalUndoCoroutineScopeModule {
    @Provides
    @Singleton
    @RoutineRemovalUndoScope
    fun provideRoutineRemovalUndoScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
