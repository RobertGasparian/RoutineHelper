package com.robertgasparian.routinehelper

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.robertgasparian.routinehelper.work.RoutineSnapshotBackfill
import com.robertgasparian.routinehelper.work.RoutineSnapshotWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class RoutineHelperApplication : Application(), Configuration.Provider {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var routineSnapshotWorkScheduler: RoutineSnapshotWorkScheduler

    @Inject
    lateinit var routineSnapshotBackfill: RoutineSnapshotBackfill

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            routineSnapshotBackfill.backfillMissedSnapshots()
        }
        routineSnapshotWorkScheduler.scheduleRecurringSnapshots()
    }
}
