package com.robertgasparian.routinehelper

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.robertgasparian.routinehelper.domain.removal.RoutineRemovalUndoCoordinator
import com.robertgasparian.routinehelper.work.RoutineSnapshotBackfill
import com.robertgasparian.routinehelper.work.RoutineSnapshotWorkScheduler
import com.robertgasparian.routinehelper.work.RoutineSummaryReminderWorkScheduler
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

    @Inject
    lateinit var routineSummaryReminderWorkScheduler: RoutineSummaryReminderWorkScheduler

    @Inject
    lateinit var routineRemovalUndoCoordinator: RoutineRemovalUndoCoordinator

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            routineSnapshotBackfill.backfillMissedSnapshots()
        }
        applicationScope.launch {
            routineRemovalUndoCoordinator.finalizeDanglingPendingRemovalsOnLaunch()
        }
        routineSnapshotWorkScheduler.scheduleRecurringSnapshots()
        routineSummaryReminderWorkScheduler.scheduleRecurringReminders()
    }
}
