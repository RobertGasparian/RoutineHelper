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
import com.robertgasparian.routinehelper.ui.app.deeplink.RoutineDeepLinks

internal class HistorySummaryReminderNotificationPublisher(
    context: Context,
) {
    private val applicationContext = context.applicationContext

    @SuppressLint("MissingPermission")
    fun publish(snapshotId: Long): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        ensureChannel()
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            RoutineDeepLinks.historySummaryEdit(snapshotId),
            applicationContext,
            MainActivity::class.java,
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            snapshotId.toNotificationId(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_summary_reminder)
            .setContentTitle(applicationContext.getString(R.string.app_summary_reminder_title))
            .setContentText(applicationContext.getString(R.string.app_summary_reminder_message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(
            snapshotId.toNotificationId(),
            notification,
        )
        return true
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

    private fun Long.toNotificationId(): Int = (this xor (this ushr Int.SIZE_BITS)).toInt()

    private companion object {
        const val CHANNEL_ID = "summary-reminders"
    }
}
