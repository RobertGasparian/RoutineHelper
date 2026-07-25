package com.robertgasparian.routinehelper.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.robertgasparian.routinehelper.MainActivity
import com.robertgasparian.routinehelper.R
import com.robertgasparian.routinehelper.domain.model.RoutineCadence
import com.robertgasparian.routinehelper.ui.app.deeplink.RoutineDeepLinks
import com.robertgasparian.routinehelper.work.RoutineSummaryReminderNotification
import com.robertgasparian.routinehelper.work.RoutineSummaryReminderNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RoutineSummaryReminderNotificationPublisher @Inject constructor(
    @ApplicationContext context: Context,
) : RoutineSummaryReminderNotifier {
    private val applicationContext = context.applicationContext

    @SuppressLint("MissingPermission")
    override fun show(notification: RoutineSummaryReminderNotification) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (!notificationManager.areNotificationsEnabled()) return

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel()
        val notificationId = notification.notificationId()
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            notification.deepLinkUri(),
            applicationContext,
            MainActivity::class.java,
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notificationContent = notification.content()
        val androidNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_summary_reminder)
            .setContentTitle(notificationContent.title)
            .setContentText(notificationContent.message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(
            notificationId,
            androidNotification,
        )
    }

    fun showSummaryEditorReminder(snapshotId: Long) {
        show(RoutineSummaryReminderNotification.WriteSummary(snapshotId))
    }

    private fun ensureChannel() {
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            applicationContext.getString(R.string.app_summary_reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun RoutineSummaryReminderNotification.deepLinkUri() =
        when (this) {
            is RoutineSummaryReminderNotification.WriteSummary ->
                RoutineDeepLinks.historySummaryEdit(snapshotId)
            is RoutineSummaryReminderNotification.BuildRoutine ->
                RoutineDeepLinks.routine(cadence)
        }

    private fun RoutineSummaryReminderNotification.content(): NotificationContent =
        when (this) {
            is RoutineSummaryReminderNotification.WriteSummary -> NotificationContent(
                title = applicationContext.getString(R.string.app_summary_reminder_title),
                message = applicationContext.getString(R.string.app_summary_reminder_message),
            )
            is RoutineSummaryReminderNotification.BuildRoutine -> when (cadence) {
                RoutineCadence.Daily -> NotificationContent(
                    title = applicationContext.getString(R.string.app_daily_routine_reminder_title),
                    message = applicationContext.getString(R.string.app_daily_routine_reminder_message),
                )
                RoutineCadence.Weekly -> NotificationContent(
                    title = applicationContext.getString(R.string.app_weekly_routine_reminder_title),
                    message = applicationContext.getString(R.string.app_weekly_routine_reminder_message),
                )
            }
        }

    private fun RoutineSummaryReminderNotification.notificationId(): Int =
        when (this) {
            is RoutineSummaryReminderNotification.WriteSummary ->
                (snapshotId xor (snapshotId ushr Int.SIZE_BITS)).toInt()
            is RoutineSummaryReminderNotification.BuildRoutine -> when (cadence) {
                RoutineCadence.Daily -> DAILY_ROUTINE_NOTIFICATION_ID
                RoutineCadence.Weekly -> WEEKLY_ROUTINE_NOTIFICATION_ID
            }
        }

    private companion object {
        const val CHANNEL_ID = "summary-reminders"
        const val DAILY_ROUTINE_NOTIFICATION_ID = Int.MIN_VALUE + 1
        const val WEEKLY_ROUTINE_NOTIFICATION_ID = Int.MIN_VALUE + 2
    }
}

private data class NotificationContent(
    val title: String,
    val message: String,
)

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RoutineSummaryReminderNotificationModule {
    @Binds
    abstract fun bindRoutineSummaryReminderNotifier(
        publisher: RoutineSummaryReminderNotificationPublisher,
    ): RoutineSummaryReminderNotifier
}
