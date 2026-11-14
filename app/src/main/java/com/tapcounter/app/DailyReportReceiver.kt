package com.tapcounter.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class DailyReportReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_DAILY_REPORT) {
            postPinned(context)
            scheduleNext(context)
        }
    }

    companion object {
        private const val CHANNEL_ID = "daily_report"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_DAILY_REPORT = "com.tapcounter.app.DAILY_REPORT"
        private const val REQUEST_CODE = 2001
        private const val HOUR = 7
        private const val MINUTE = 30

        fun postPinned(context: Context) {
            val db = TapDatabase.getInstance(context)
            val today = db.getToday()
            val screen = db.getScreenStats(TapDatabase.todayKey())

            val body = context.getString(
                R.string.pinned_body,
                today.clicks, today.swipes, today.touches,
                screen.wakeCount, formatDuration(screen.activeSeconds)
            )

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.pinned_channel_name),
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
            }

            val contentIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(context.getString(R.string.pinned_title))
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setOngoing(true)
                .setShowWhen(false)
                .setContentIntent(contentIntent)
                .build()

            nm.notify(NOTIFICATION_ID, notification)
        }

        private fun formatDuration(seconds: Long): String {
            if (seconds < 60) return "${seconds}秒"
            val hours = seconds / 3600
            val mins = (seconds % 3600) / 60
            return if (hours > 0) "${hours}小时${mins}分" else "${mins}分"
        }

        fun scheduleNext(context: Context) {
            try {
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, DailyReportReceiver::class.java)
                    .setAction(ACTION_DAILY_REPORT)
                val pi = PendingIntent.getBroadcast(
                    context, REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val now = Calendar.getInstance()
                val next = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, HOUR)
                    set(Calendar.MINUTE, MINUTE)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
                }
                if (Build.VERSION.SDK_INT >= 31 && am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, next.timeInMillis, pi
                    )
                } else {
                    am.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, next.timeInMillis, pi
                    )
                }
            } catch (_: Exception) {
            }
        }
    }
}
