package com.tapcounter.app

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SessionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.styleOf(ThemeManager.current(this)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions)

        WallpaperManager.applyToRoot(this, WallpaperManager.KIND_SUB)

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.listContainer)
        val sessions = TapDatabase.getInstance(this).getSessions()

        if (sessions.isEmpty()) {
            val tv = TextView(this).apply {
                text = getString(R.string.no_sessions)
                textSize = 14f
                setTextColor(ThemeManager.color(this@SessionsActivity, R.attr.themeTextSecondary))
                gravity = Gravity.CENTER
                setPadding(0, 24, 0, 24)
            }
            container.addView(tv)
            return
        }

        for (s in sessions) {
            container.addView(makeSessionCard(s))
        }
    }

    private fun makeSessionCard(s: TapDatabase.SessionInfo): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 14, 0, 14)
        }

        val tvRange = TextView(this).apply {
            text = "${s.startTime}  →  ${s.endTime}"
            textSize = 14f
            setTextColor(ThemeManager.color(this@SessionsActivity, R.attr.themeTextPrimary))
        }
        val tvData = TextView(this).apply {
            text = "点击 ${s.clicks} · 滑动 ${s.swipes} · 碰屏 ${s.touches}"
            textSize = 13f
            setTextColor(ThemeManager.color(this@SessionsActivity, R.attr.themeTextSecondary))
            setPadding(0, 4, 0, 0)
        }
        card.addView(tvRange)
        card.addView(tvData)

        val divider = TextView(this).apply {
            text = ""
            setBackgroundColor(ThemeManager.color(this@SessionsActivity, R.attr.themeTextSecondary))
            alpha = 0.2f
        }
        card.addView(divider, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1
        ).apply { topMargin = 14 })

        return card
    }
}
