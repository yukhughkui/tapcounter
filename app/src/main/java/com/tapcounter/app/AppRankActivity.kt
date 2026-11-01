package com.tapcounter.app

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AppRankActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.styleOf(ThemeManager.current(this)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_rank)

        WallpaperManager.applyToRoot(this, WallpaperManager.KIND_SUB)

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.listContainer)
        // 只列出能解析出真实名称的应用；无来源(unknown)与解析失败(系统组件/已卸载等)直接不展示
        val stats = TapDatabase.getInstance(this)
            .getAppRank(TapDatabase.todayKey())
            .filter { it.packageName != "unknown" && canResolve(it.packageName) }

        if (stats.isEmpty()) {
            val tv = TextView(this).apply {
                text = getString(R.string.no_app_data)
                textSize = 14f
                setTextColor(ThemeManager.color(this@AppRankActivity, R.attr.themeTextSecondary))
                gravity = Gravity.CENTER
                setPadding(0, 24, 0, 24)
            }
            container.addView(tv)
            return
        }

        for (stat in stats) {
            container.addView(makeRow(stat))
        }
    }

    private fun canResolve(packageName: String): Boolean =
        try {
            packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }

    private fun makeRow(stat: TapDatabase.AppStat): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 10, 0, 10)
        }

        val icon = ImageView(this)
        val iconDrawable: Drawable? = try {
            packageManager.getApplicationIcon(stat.packageName)
        } catch (_: Exception) {
            resources.getDrawable(android.R.drawable.sym_def_app_icon, null)
        }
        icon.setImageDrawable(iconDrawable)

        val nameText = if (stat.packageName == "unknown") {
            "其他/未知"
        } else {
            try {
                val info = packageManager.getApplicationInfo(stat.packageName, 0)
                packageManager.getApplicationLabel(info).toString().ifBlank { "未知应用" }
            } catch (_: Exception) {
                "未知应用"
            }
        }

        val tvName = TextView(this).apply {
            text = nameText
            textSize = 14f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(ThemeManager.color(this@AppRankActivity, R.attr.themeTextPrimary))
        }
        val tvClicks = cell(stat.clicks.toString())
        val tvSwipes = cell(stat.swipes.toString())
        val tvTouches = cell((stat.clicks + stat.swipes).toString())

        row.addView(
            icon,
            LinearLayout.LayoutParams(dp(38), dp(38))
        )
        row.addView(tvName, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f))
        row.addView(tvClicks, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f))
        row.addView(tvSwipes, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f))
        row.addView(tvTouches, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f))
        return row
    }

    private fun cell(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 13f
        gravity = Gravity.END
        setTextColor(ThemeManager.color(this@AppRankActivity, R.attr.themeTextSecondary))
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
