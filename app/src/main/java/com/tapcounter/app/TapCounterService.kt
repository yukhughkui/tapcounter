package com.tapcounter.app

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.accessibility.AccessibilityEvent
import java.util.concurrent.Executors
import kotlin.concurrent.Volatile

class TapCounterService : AccessibilityService() {

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var scrollCountedInGesture = false

    private var lastScrollTime = 0L

    @Volatile
    private var screenOnTimestamp = 0L

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> onScreenOn()
                Intent.ACTION_SCREEN_OFF -> onScreenOff()
            }
        }
    }

    private val screenTickRunnable = object : Runnable {
        override fun run() {
            flushActiveSeconds()
            handler.postDelayed(this, 60000)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isInteractive) screenOnTimestamp = System.currentTimeMillis()
        handler.postDelayed(screenTickRunnable, 60000)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED ->
                record(event.packageName?.toString(), TapDatabase.TYPE_CLICK)

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                val now = System.currentTimeMillis()
                if (!scrollCountedInGesture || now - lastScrollTime > 800) {
                    scrollCountedInGesture = true
                    record(event.packageName?.toString(), TapDatabase.TYPE_SWIPE)
                }
                lastScrollTime = now
            }
        }
    }

    private fun record(packageName: String?, type: Int) {
        val db = TapDatabase.getInstance(this)
        val pkg = if (packageName.isNullOrBlank()) "unknown" else packageName
        executor.execute {
            db.onCounted(type)
            db.onCounted(TapDatabase.TYPE_TOUCH)
            db.addAppTap(TapDatabase.todayKey(), pkg, type)
        }
    }

    private fun onScreenOn() {
        if (screenOnTimestamp == 0L) {
            executor.execute {
                TapDatabase.getInstance(this@TapCounterService).addWake(TapDatabase.todayKey())
            }
        }
        screenOnTimestamp = System.currentTimeMillis()
        flushActiveSeconds()
    }

    private fun onScreenOff() {
        flushActiveSeconds()
        screenOnTimestamp = 0L
    }

    private fun flushActiveSeconds() {
        val now = System.currentTimeMillis()
        if (screenOnTimestamp > 0 && now > screenOnTimestamp) {
            val secs = ((now - screenOnTimestamp) / 1000).toInt()
            if (secs > 0) {
                executor.execute {
                    TapDatabase.getInstance(this@TapCounterService)
                        .addActiveSeconds(TapDatabase.todayKey(), secs)
                }
            }
            screenOnTimestamp = now
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        flushActiveSeconds()
        handler.removeCallbacksAndMessages(null)
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {
        }
        executor.shutdown()
        super.onDestroy()
    }
}
