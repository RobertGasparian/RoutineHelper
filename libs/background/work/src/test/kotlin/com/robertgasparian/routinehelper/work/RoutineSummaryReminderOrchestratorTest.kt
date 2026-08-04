package com.robertgasparian.routinehelper.work

import com.robertgasparian.routinehelper.core.testing.FixedTimeProvider
import com.robertgasparian.routinehelper.domain.model.AppSettings
import com.robertgasparian.routinehelper.domain.model.ReflectionRating
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshot
import com.robertgasparian.routinehelper.domain.model.RoutineSnapshotSummary
import com.robertgasparian.routinehelper.domain.model.SelectedReflectionTag
import com.robertgasparian.routinehelper.domain.model.TodayRoutineItem
import com.robertgasparian.routinehelper.domain.model.WeeklyRoutineItem
import com.robertgasparian.routinehelper.domain.repository.FakeRoutineHistoryRepository
import com.robertgasparian.routinehelper.domain.repository.FakeTodayRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.FakeWeeklyRoutineRepository
import com.robertgasparian.routinehelper.domain.repository.SettingsRepository
import com.robertgasparian.routinehelper.domain.usecase.AppSettingsUseCase
import com.robertgasparian.routinehelper.domain.usecase.FinalizeTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.FinalizeWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetTodayUseCase
import com.robertgasparian.routinehelper.domain.usecase.ResetWeeklyUseCase
import com.robertgasparian.routinehelper.domain.usecase.SnapshotSummariesUseCase
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineSummaryReminderOrchestratorTest {
    private val zoneId = ZoneId.of("America/New_York")
    private val now = ZonedDateTime.of(2026, 6, 8, 10, 0, 0, 0, zoneId)
    private val historyRepository = FakeRoutineHistoryRepository()
    private val todayRepository = FakeTodayRoutineRepository()
    private val weeklyRepository = FakeWeeklyRoutineRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val notifier = RecordingRoutineSummaryReminderNotifier()
    private val timeProvider = FixedTimeProvider(now.toInstant(), zoneId)
    private val snapshotSummariesUseCase = SnapshotSummariesUseCase(historyRepository)
    private val orchestrator = RoutineSummaryReminderOrchestrator(
        appSettingsUseCase = AppSettingsUseCase(settingsRepository),
        snapshotSummariesUseCase = snapshotSummariesUseCase,
        dailySnapshotOrchestrator = DailySnapshotOrchestrator(
            finalizeTodayUseCase = FinalizeTodayUseCase(todayRepository, historyRepository),
            resetTodayUseCase = ResetTodayUseCase(todayRepository),
            snapshotSummariesUseCase = snapshotSummariesUseCase,
            timeProvider = timeProvider,
        ),
        weeklySnapshotOrchestrator = WeeklySnapshotOrchestrator(
            finalizeWeeklyUseCase = FinalizeWeeklyUseCase(weeklyRepository, historyRepository),
            resetWeeklyUseCase = ResetWeeklyUseCase(weeklyRepository),
            snapshotSummariesUseCase = snapshotSummariesUseCase,
            timeProvider = timeProvider,
        ),
        notifier = notifier,
        timeProvider = timeProvider,
    )

    @Test
    fun `given daily reminders enabled and previous day snapshot exists when sending then summary reminder is shown`() =
        runTest {
            settingsRepository.setSettings(
                AppSettings(isDailySummaryNotificationEnabled = true),
            )
            historyRepository.setSnapshot(
                snapshotSummary(
                    snapshotId = 42L,
                    periodStartDate = "2026-06-07",
                    cadence = RoutineCadence.Daily,
                ),
            )

            orchestrator.sendDailyReminder(now)

            assertEquals(
                listOf(RoutineSummaryReminderNotification.WriteSummary(snapshotId = 42L)),
                notifier.notifications,
            )
        }

    @Test
    fun `given daily reminders enabled and previous day snapshot is absent when sending then daily routine reminder is shown`() =
        runTest {
            settingsRepository.setSettings(
                AppSettings(isDailySummaryNotificationEnabled = true),
            )

            orchestrator.sendDailyReminder(now)

            assertEquals(
                listOf(
                    RoutineSummaryReminderNotification.BuildRoutine(RoutineCadence.Daily),
                ),
                notifier.notifications,
            )
        }

    @Test
    fun `given daily snapshot worker has not run when sending then snapshot is finalized before reminder selection`() =
        runTest {
            settingsRepository.setSettings(
                AppSettings(isDailySummaryNotificationEnabled = true),
            )
            todayRepository.setItems(
                date = "2026-06-07",
                items = listOf(todayItem(date = "2026-06-07")),
            )

            orchestrator.sendDailyReminder(now)

            assertEquals(
                listOf(RoutineSummaryReminderNotification.WriteSummary(snapshotId = 1L)),
                notifier.notifications,
            )
            assertEquals(listOf("2026-06-07"), todayRepository.resetDates)
        }

    @Test
    fun `given daily snapshot has a note when sending then no reminder is shown`() = runTest {
        settingsRepository.setSettings(
            AppSettings(isDailySummaryNotificationEnabled = true),
        )
        historyRepository.setSnapshot(
            snapshotSummary(
                snapshotId = 42L,
                periodStartDate = "2026-06-07",
                cadence = RoutineCadence.Daily,
                hasSummaryNote = true,
            ),
        )

        orchestrator.sendDailyReminder(now)

        assertTrue(notifier.notifications.isEmpty())
    }

    @Test
    fun `given daily snapshot has a rating when sending then no reminder is shown`() = runTest {
        settingsRepository.setSettings(
            AppSettings(isDailySummaryNotificationEnabled = true),
        )
        historyRepository.setSnapshot(
            RoutineSnapshot(
                snapshotId = 42L,
                periodStartDate = "2026-06-07",
                finalizedAtMillis = now.toInstant().toEpochMilli(),
                cadence = RoutineCadence.Daily,
                rating = ReflectionRating(4),
                items = emptyList(),
            ),
        )

        orchestrator.sendDailyReminder(now)

        assertTrue(notifier.notifications.isEmpty())
    }

    @Test
    fun `given weekly reminders enabled and previous week snapshot exists when sending then summary reminder is shown`() =
        runTest {
            settingsRepository.setSettings(
                AppSettings(isWeeklySummaryNotificationEnabled = true),
            )
            historyRepository.setSnapshot(
                snapshotSummary(
                    snapshotId = 84L,
                    periodStartDate = "2026-06-01",
                    cadence = RoutineCadence.Weekly,
                ),
            )

            orchestrator.sendWeeklyReminder(now)

            assertEquals(
                listOf(RoutineSummaryReminderNotification.WriteSummary(snapshotId = 84L)),
                notifier.notifications,
            )
        }

    @Test
    fun `given weekly reminders enabled and previous week snapshot is absent when sending then weekly routine reminder is shown`() =
        runTest {
            settingsRepository.setSettings(
                AppSettings(isWeeklySummaryNotificationEnabled = true),
            )

            orchestrator.sendWeeklyReminder(now)

            assertEquals(
                listOf(
                    RoutineSummaryReminderNotification.BuildRoutine(RoutineCadence.Weekly),
                ),
                notifier.notifications,
            )
        }

    @Test
    fun `given weekly snapshot worker has not run when sending then snapshot is finalized before reminder selection`() =
        runTest {
            settingsRepository.setSettings(
                AppSettings(isWeeklySummaryNotificationEnabled = true),
            )
            weeklyRepository.setItems(
                weekStartDate = "2026-06-01",
                items = listOf(weeklyItem(weekStartDate = "2026-06-01")),
            )

            orchestrator.sendWeeklyReminder(now)

            assertEquals(
                listOf(RoutineSummaryReminderNotification.WriteSummary(snapshotId = 1L)),
                notifier.notifications,
            )
            assertEquals(listOf("2026-06-01"), weeklyRepository.resetWeeks)
        }

    @Test
    fun `given weekly snapshot has a note when sending then no reminder is shown`() = runTest {
        settingsRepository.setSettings(
            AppSettings(isWeeklySummaryNotificationEnabled = true),
        )
        historyRepository.setSnapshot(
            snapshotSummary(
                snapshotId = 84L,
                periodStartDate = "2026-06-01",
                cadence = RoutineCadence.Weekly,
                hasSummaryNote = true,
            ),
        )

        orchestrator.sendWeeklyReminder(now)

        assertTrue(notifier.notifications.isEmpty())
    }

    @Test
    fun `given weekly snapshot has a selected tag when sending then no reminder is shown`() = runTest {
        settingsRepository.setSettings(
            AppSettings(isWeeklySummaryNotificationEnabled = true),
        )
        historyRepository.setSnapshot(
            RoutineSnapshot(
                snapshotId = 84L,
                periodStartDate = "2026-06-01",
                finalizedAtMillis = now.toInstant().toEpochMilli(),
                cadence = RoutineCadence.Weekly,
                selectedTags = listOf(SelectedReflectionTag(label = "Balanced", position = 0)),
                items = emptyList(),
            ),
        )

        orchestrator.sendWeeklyReminder(now)

        assertTrue(notifier.notifications.isEmpty())
    }

    @Test
    fun `given reminders disabled when sending then no notification is shown`() = runTest {
        orchestrator.sendDailyReminder(now)
        orchestrator.sendWeeklyReminder(now)

        assertTrue(notifier.notifications.isEmpty())
        assertTrue(todayRepository.resetDates.isEmpty())
        assertTrue(weeklyRepository.resetWeeks.isEmpty())
    }

    private fun snapshotSummary(
        snapshotId: Long,
        periodStartDate: String,
        cadence: RoutineCadence,
        hasSummaryNote: Boolean = false,
    ): RoutineSnapshotSummary =
        RoutineSnapshotSummary(
            snapshotId = snapshotId,
            periodStartDate = periodStartDate,
            finalizedAtMillis = now.toInstant().toEpochMilli(),
            cadence = cadence,
            totalCount = 1,
            hasSummaryNote = hasSummaryNote,
        )

    private fun todayItem(date: String): TodayRoutineItem =
        TodayRoutineItem(
            routineItemId = 10L,
            actionId = 100L,
            title = "Drink water",
            description = null,
            position = 0,
            date = date,
            isChecked = true,
            note = null,
        )

    private fun weeklyItem(weekStartDate: String): WeeklyRoutineItem =
        WeeklyRoutineItem(
            routineItemId = 20L,
            actionId = 200L,
            title = "Review goals",
            description = null,
            position = 0,
            weekStartDate = weekStartDate,
            isChecked = true,
            note = null,
        )
}

private class FakeSettingsRepository : SettingsRepository {
    private val mutableSettings = MutableStateFlow(AppSettings())

    override val settings: Flow<AppSettings> = mutableSettings

    fun setSettings(settings: AppSettings) {
        mutableSettings.value = settings
    }

    override suspend fun setDailySummaryNotificationEnabled(isEnabled: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(
            isDailySummaryNotificationEnabled = isEnabled,
        )
    }

    override suspend fun setWeeklySummaryNotificationEnabled(isEnabled: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(
            isWeeklySummaryNotificationEnabled = isEnabled,
        )
    }
}

private class RecordingRoutineSummaryReminderNotifier : RoutineSummaryReminderNotifier {
    val notifications = mutableListOf<RoutineSummaryReminderNotification>()

    override fun show(notification: RoutineSummaryReminderNotification) {
        notifications += notification
    }
}
