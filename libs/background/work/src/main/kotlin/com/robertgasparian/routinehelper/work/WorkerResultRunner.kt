package com.robertgasparian.routinehelper.work

import androidx.work.ListenableWorker
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class WorkerResultRunner @Inject constructor() {
    suspend fun run(block: suspend () -> Unit): ListenableWorker.Result =
        try {
            block()
            ListenableWorker.Result.success()
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (_: Exception) {
            ListenableWorker.Result.retry()
        }
}
