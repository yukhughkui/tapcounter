package com.tapcounter.app

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var tvTodayClicks: TextView
    private lateinit var tvTodaySwipes: TextView
    private lateinit var tvTodayTouches: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvScreenStats: TextView
    private lateinit var tvServiceStatus: TextView
    private lateinit var btnEnableService: MaterialButton
    private lateinit var tvSessionStart: TextView
    private lateinit var tvSessionClicks: TextView
    private lateinit var tvSessionSwipes: TextView
    private lateinit var tvSessionTouches: TextView

    private val handler = Handler(Looper.getMainLooper())

    private val wallpaperMainLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleWallpaperResult(uri, WallpaperManager.KIND_MAIN)
        }

    private val wallpaperSubLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleWallpaperResult(uri, WallpaperManager.KIND_SUB)
        }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.styleOf(ThemeManager.current(this)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WallpaperManager.applyWindow(this, WallpaperManager.KIND_MAIN)

        tvTodayClicks = findViewById(R.id.tvTodayClicks)
        tvTodaySwipes = findViewById(R.id.tvTodaySwipes)
        tvTodayTouches = findViewById(R.id.tvTodayTouches)
        tvTotal = findViewById(R.id.tvTotal)
        tvScreenStats = findViewById(R.id.tvScreenStats)
        tvServiceStatus = findViewById(R.id.tvServiceStatus)
        btnEnableService = findViewById(R.id.btnEnableService)
        tvSessionStart = findViewById(R.id.tvSessionStart)
        tvSessionClicks = findViewById(R.id.tvSessionClicks)
        tvSessionSwipes = findViewById(R.id.tvSessionSwipes)
        tvSessionTouches = findViewById(R.id.tvSessionTouches)

        findViewById<MaterialButton>(R.id.btnTheme).setOnClickListener {
            showThemeDialog()
        }

        btnEnableService.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<MaterialButton>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnSessions).setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnAppRank).setOnClickListener {
            startActivity(Intent(this, AppRankActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnSaveSession).setOnClickListener {
            TapDatabase.getInstance(this).saveCurrentSession()
            Toast.makeText(this, getString(R.string.session_saved), Toast.LENGTH_SHORT).show()
            refresh()
        }

        DailyReportReceiver.scheduleNext(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
        handler.postDelayed(refreshRunnable, 1000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun refresh() {
        val db = TapDatabase.getInstance(this)

        val today = db.getToday()
        tvTodayClicks.text = today.clicks.toString()
        tvTodaySwipes.text = today.swipes.toString()
        tvTodayTouches.text = today.touches.toString()

        val total = db.getTotal()
        tvTotal.text = "累计（全部时间）：点击 ${total.clicks} · 滑动 ${total.swipes} · 碰屏 ${total.touches}"

        val screen = db.getScreenStats(TapDatabase.todayKey())
        tvScreenStats.text = "亮屏 ${screen.wakeCount} 次 · 累计 ${formatDuration(screen.activeSeconds)}"

        val session = db.getCurrentSession()
        tvSessionStart.text = "本段开始：${session.startTime}"
        tvSessionClicks.text = session.clicks.toString()
        tvSessionSwipes.text = session.swipes.toString()
        tvSessionTouches.text = session.touches.toString()

        val on = isServiceEnabled()
        tvServiceStatus.text = if (on) getString(R.string.service_status_on) else getString(R.string.service_status_off)
        tvServiceStatus.setTextColor(
            getColor(if (on) android.R.color.holo_green_dark else android.R.color.holo_red_dark)
        )
        btnEnableService.text = if (on) "已开启（点此可管理）" else getString(R.string.enable_service)
    }

    private fun isServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val list = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val myName = TapCounterService::class.java.name
        val myShort = "." + TapCounterService::class.java.simpleName
        return list.any {
            it.resolveInfo.serviceInfo.packageName == packageName &&
                (it.resolveInfo.serviceInfo.name == myName || it.resolveInfo.serviceInfo.name == myShort)
        }
    }

    private fun formatDuration(seconds: Long): String {
        if (seconds < 60) return "${seconds}秒"
        val hours = seconds / 3600
        val mins = (seconds % 3600) / 60
        return if (hours > 0) "${hours}小时${mins}分" else "${mins}分"
    }

    private fun handleWallpaperResult(uri: Uri?, kind: String) {
        if (uri == null) return
        if (WallpaperManager.save(this, uri, kind)) {
            if (kind == WallpaperManager.KIND_MAIN) {
                WallpaperManager.applyWindow(this, kind)
            }
            Toast.makeText(this, getString(R.string.wallpaper_saved), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.wallpaper_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetWallpaper(kind: String) {
        WallpaperManager.delete(this, kind)
        if (kind == WallpaperManager.KIND_MAIN) {
            recreate()
        } else {
            Toast.makeText(this, getString(R.string.wallpaper_reset_done), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWallpaperDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.wallpaper_dialog_title))
            .setItems(
                arrayOf(
                    getString(R.string.wallpaper_main),
                    getString(R.string.wallpaper_sub),
                    getString(R.string.wallpaper_reset_main),
                    getString(R.string.wallpaper_reset_sub)
                )
            ) { _, which ->
                when (which) {
                    0 -> wallpaperMainLauncher.launch("image/*")
                    1 -> wallpaperSubLauncher.launch("image/*")
                    2 -> resetWallpaper(WallpaperManager.KIND_MAIN)
                    3 -> resetWallpaper(WallpaperManager.KIND_SUB)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showThemeDialog() {
        val names = arrayOf(
            getString(R.string.theme_business),
            getString(R.string.theme_kitty),
            getString(R.string.theme_genshin),
            getString(R.string.theme_pony),
            getString(R.string.theme_green)
        )
        val values = arrayOf(
            ThemeManager.BUSINESS,
            ThemeManager.KITTY,
            ThemeManager.GENSHIN,
            ThemeManager.PONY,
            ThemeManager.GREEN
        )
        val current = ThemeManager.current(this)
        val selected = values.indexOf(current).coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.theme_dialog_title))
            .setSingleChoiceItems(names, selected) { dialog, which ->
                ThemeManager.save(this, values[which])
                dialog.dismiss()
                recreate()
            }
            .setNeutralButton(getString(R.string.wallpaper_dialog_title)) { _, _ ->
                showWallpaperDialog()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
