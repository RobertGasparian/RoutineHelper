package com.robertgasparian.routinehelper.notification

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import com.robertgasparian.routinehelper.R
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.app.deeplink.RoutineDeepLinks
import com.robertgasparian.routinehelper.work.RoutineSummaryReminderNotification
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = Application::class)
class RoutineSummaryReminderNotificationPublisherTest {
    private lateinit var application: Application
    private lateinit var notificationManager: NotificationManager
    private lateinit var shadowNotificationManager: ShadowNotificationManager
    private lateinit var publisher: RoutineSummaryReminderNotificationPublisher

    @Before
    fun setUp() {
        application = RuntimeEnvironment.getApplication()
        notificationManager = application.getSystemService(NotificationManager::class.java)
        shadowNotificationManager = shadowOf(notificationManager)
        publisher = RoutineSummaryReminderNotificationPublisher(application)
    }

    @After
    fun tearDown() {
        notificationManager.cancelAll()
    }

    @Test
    fun `given daily routine reminder when showing then notification opens Daily tab deep link`() {
        publisher.show(
            RoutineSummaryReminderNotification.BuildRoutine(RoutineCadence.Daily),
        )

        val notification = shadowNotificationManager.allNotifications.single()
        assertEquals(
            application.getString(R.string.app_daily_routine_reminder_title),
            notification.extras.getString(Notification.EXTRA_TITLE),
        )
        assertEquals(
            RoutineDeepLinks.routine(RoutineCadence.Daily),
            shadowOf(notification.contentIntent).savedIntent.data,
        )
    }

    @Test
    fun `given weekly routine reminder when showing then notification opens Weekly tab deep link`() {
        publisher.show(
            RoutineSummaryReminderNotification.BuildRoutine(RoutineCadence.Weekly),
        )

        val notification = shadowNotificationManager.allNotifications.single()
        assertEquals(
            application.getString(R.string.app_weekly_routine_reminder_title),
            notification.extras.getString(Notification.EXTRA_TITLE),
        )
        assertEquals(
            RoutineDeepLinks.routine(RoutineCadence.Weekly),
            shadowOf(notification.contentIntent).savedIntent.data,
        )
    }

    @Test
    fun `given daily and weekly routine reminders when showing then both remain visible`() {
        publisher.show(
            RoutineSummaryReminderNotification.BuildRoutine(RoutineCadence.Daily),
        )
        publisher.show(
            RoutineSummaryReminderNotification.BuildRoutine(RoutineCadence.Weekly),
        )

        assertEquals(2, shadowNotificationManager.allNotifications.size)
    }

    @Test
    fun `given snapshot reminder when showing then notification opens summary editor deep link`() {
        publisher.show(
            RoutineSummaryReminderNotification.WriteSummary(snapshotId = 42L),
        )

        val notification = shadowNotificationManager.allNotifications.single()
        assertEquals(
            application.getString(R.string.app_summary_reminder_title),
            notification.extras.getString(Notification.EXTRA_TITLE),
        )
        assertEquals(
            RoutineDeepLinks.historySummaryEdit(snapshotId = 42L),
            shadowOf(notification.contentIntent).savedIntent.data,
        )
    }

    @Test
    fun `given app notifications are disabled when showing then no notification is posted`() {
        shadowNotificationManager.setNotificationsEnabled(false)

        publisher.show(
            RoutineSummaryReminderNotification.BuildRoutine(RoutineCadence.Daily),
        )

        assertTrue(shadowNotificationManager.allNotifications.isEmpty())
    }
}
