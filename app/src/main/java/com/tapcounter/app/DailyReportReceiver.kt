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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 每天早上 08:00 发送昨日数据报告通知（点击/滑动/总次数 + 温馨建议），
 * 并在触发后自动安排第二天早上的下一次提醒。
 */
class DailyReportReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_DAILY_REPORT) {
            postDailyReport(context)
            scheduleNext(context)
        }
    }

    private fun postDailyReport(context: Context) {
        val db = TapDatabase.getInstance(context)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time)
        val rec = db.getDay(yesterday)

        val title = context.getString(R.string.notif_title)
        val body = if (rec == null || (rec.clicks + rec.swipes + rec.touches) <= 0) {
            context.getString(R.string.notif_no_data)
        } else {
            val total = rec.clicks + rec.swipes + rec.touches
            val tip = if (total >= 5000) {
                context.getString(R.string.notif_tip_high)
            } else {
                context.getString(R.string.notif_tip_ok)
            }
            context.getString(
                R.string.notif_body,
                rec.clicks, rec.swipes, total, tip
            )
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
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
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "daily_report"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_DAILY_REPORT = "com.tapcounter.app.DAILY_REPORT"
        private const val REQUEST_CODE = 2001
        private const val HOUR = 8
        private const val MINUTE = 0

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
                // Android 12+ 精确闹钟需 SCHEDULE_EXACT_ALARM 权限且可能未被授予；
                // 有权限用精确，没有则降级为免权限的普通定时（每日早报允许几分钟偏差）
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
                // 定时注册失败绝不影响 App 启动与计数
            }
        }
    }
}
