package com.robertgasparian.routinehelper.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteSnapshotUseCaseTest {
    private val repository = FakeRoutineHistoryRepository()
    private val useCase = DeleteSnapshotUseCase(repository)

    @Test
    fun deletesSnapshotById() = runTest {
        useCase(snapshotId = 42L)

        assertEquals(listOf(42L), repository.deletedSnapshotIds)
    }
}
