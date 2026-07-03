package com.robertgasparian.routinehelper.work

import androidx.work.ListenableWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class WorkerResultRunnerTest {
    private val runner = WorkerResultRunner()

    @Test
    fun `when worker block completes then success result is returned`() = runTest {
        val result = runner.run {
            // No-op.
        }

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `given worker block throws exception when run then retry result is returned`() = runTest {
        val result = runner.run {
            error("temporary failure")
        }

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `given worker block is cancelled when run then cancellation is rethrown`() = runTest {
        val cancellationException = CancellationException("cancelled")

        try {
            runner.run {
                throw cancellationException
            }
            fail("Expected cancellation to be rethrown")
        } catch (actual: CancellationException) {
            assertSame(cancellationException, actual)
        }
    }
}
