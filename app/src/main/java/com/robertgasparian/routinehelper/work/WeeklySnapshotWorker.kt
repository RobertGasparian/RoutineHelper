package com.robertgasparian.routinehelper.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetWeeklyUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.ZonedDateTime
import kotlinx.coroutines.CancellationException

@HiltWorker
class WeeklySnapshotWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val finalizeWeeklyUseCase: FinalizeWeeklyUseCase,
    private val resetWeeklyUseCase: ResetWeeklyUseCase,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val snapshotWeekStartDate = SnapshotWorkDates.previousCompletedCalendarWeekStartDate(ZonedDateTime.now()).toString()
        return try {
            finalizeWeeklyUseCase(
                weekStartDate = snapshotWeekStartDate,
                snapshotWeekStartDate = snapshotWeekStartDate,
                finalizedAtMillis = System.currentTimeMillis(),
            )
            resetWeeklyUseCase(snapshotWeekStartDate)
            Result.success()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
