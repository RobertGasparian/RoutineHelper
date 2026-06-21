package com.robertgasparian.routinehelper.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.robertgasparian.routinehelper.core.time.TimeProvider
import com.robertgasparian.routinehelper.domain.time.SnapshotDates
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetTodayUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

@HiltWorker
class DailySnapshotWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val finalizeTodayUseCase: FinalizeTodayUseCase,
    private val resetTodayUseCase: ResetTodayUseCase,
    private val timeProvider: TimeProvider,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val snapshotDate = SnapshotDates.dailySnapshotDate(timeProvider.now()).toString()
        return try {
            finalizeTodayUseCase(
                date = snapshotDate,
                snapshotDate = snapshotDate,
                finalizedAtMillis = timeProvider.currentTimeMillis(),
            )
            resetTodayUseCase(snapshotDate)
            Result.success()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
