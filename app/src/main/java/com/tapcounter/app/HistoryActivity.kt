package com.tapcounter.app

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.styleOf(ThemeManager.current(this)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        listContainer = findViewById(R.id.listContainer)

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }

        val db = TapDatabase.getInstance(this)
        val history = buildLastNDays(30, db)

        listContainer.removeAllViews()
        for (r in history.reversed()) {
            listContainer.addView(makeRow(r))
        }
    }

    private fun buildLastNDays(days: Int, db: TapDatabase): List<TapDatabase.DayRecord> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val map = db.getHistory(days).associateBy { it.date }
        val result = mutableListOf<TapDatabase.DayRecord>()
        val cal = Calendar.getInstance()
        for (i in days - 1 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val key = fmt.format(cal.time)
            val rec = map[key]
            result.add(if (rec != null) rec else TapDatabase.DayRecord(key, 0, 0, 0))
        }
        return result
    }

    private fun makeRow(r: TapDatabase.DayRecord): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 10, 0, 10)
        }
        val tvDate = TextView(this).apply {
            text = r.date
            textSize = 13f
            setTextColor(ThemeManager.color(this@HistoryActivity, R.attr.themeTextPrimary))
        }
        val tvClicks = cell(r.clicks.toString())
        val tvSwipes = cell(r.swipes.toString())
        val tvTouches = cell(r.touches.toString())
        row.addView(tvDate, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f))
        row.addView(tvClicks, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(tvSwipes, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(tvTouches, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        return row
    }

    private fun cell(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 13f
        gravity = Gravity.END
        setTextColor(ThemeManager.color(this@HistoryActivity, R.attr.themeTextSecondary))
    }
}
