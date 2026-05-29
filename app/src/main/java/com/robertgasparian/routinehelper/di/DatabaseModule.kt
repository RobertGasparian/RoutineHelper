package com.robertgasparian.routinehelper.di

import android.content.Context
import androidx.room.Room
import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideRoutineDatabase(
        @ApplicationContext context: Context,
    ): RoutineDatabase =
        Room.databaseBuilder(
            context,
            RoutineDatabase::class.java,
            "routine-helper.db",
        ).build()
}
