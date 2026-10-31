package com.tapcounter.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TapDatabase private constructor(context: Context) : SQLiteOpenHelper(context, "taps.db", null, 3) {

    data class DayRecord(val date: String, val clicks: Long, val swipes: Long, val touches: Long)

    data class SessionInfo(
        val startTime: String,
        val endTime: String,
        val clicks: Long,
        val swipes: Long,
        val touches: Long
    )

    data class AppStat(val packageName: String, val clicks: Long, val swipes: Long)

    data class ScreenStats(val wakeCount: Long, val activeSeconds: Long)

    companion object {
        const val TYPE_CLICK = 0
        const val TYPE_SWIPE = 1
        const val TYPE_TOUCH = 2

        @Volatile
        private var instance: TapDatabase? = null

        fun getInstance(context: Context): TapDatabase =
            instance ?: synchronized(this) {
                instance ?: TapDatabase(context.applicationContext).also { instance = it }
            }

        fun todayKey(): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        fun nowStamp(): String =
            SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS taps (" +
                "date TEXT PRIMARY KEY, " +
                "clicks INTEGER NOT NULL DEFAULT 0, " +
                "swipes INTEGER NOT NULL DEFAULT 0, " +
                "touches INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS current_session (" +
                "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                "start_time TEXT NOT NULL, " +
                "clicks INTEGER NOT NULL DEFAULT 0, " +
                "swipes INTEGER NOT NULL DEFAULT 0, " +
                "touches INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "INSERT OR IGNORE INTO current_session(id, start_time) VALUES(1, ?)",
            arrayOf(nowStamp())
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "start_time TEXT NOT NULL, " +
                "end_time TEXT NOT NULL, " +
                "clicks INTEGER NOT NULL, " +
                "swipes INTEGER NOT NULL, " +
                "touches INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS app_taps (" +
                "date TEXT NOT NULL, " +
                "package TEXT NOT NULL, " +
                "clicks INTEGER NOT NULL DEFAULT 0, " +
                "swipes INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(date, package))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS screen_stats (" +
                "date TEXT PRIMARY KEY, " +
                "wake_count INTEGER NOT NULL DEFAULT 0, " +
                "active_seconds INTEGER NOT NULL DEFAULT 0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val cols = db.rawQuery("PRAGMA table_info(taps)", null).use { c ->
            val names = mutableSetOf<String>()
            while (c.moveToNext()) names.add(c.getString(1))
            names
        }
        if (!cols.contains("clicks")) {
            db.execSQL("ALTER TABLE taps RENAME TO taps_old")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS taps (" +
                    "date TEXT PRIMARY KEY, " +
                    "clicks INTEGER NOT NULL DEFAULT 0, " +
                    "swipes INTEGER NOT NULL DEFAULT 0, " +
                    "touches INTEGER NOT NULL DEFAULT 0)"
            )
            if (cols.contains("count")) {
                db.execSQL(
                    "INSERT INTO taps(date, clicks, swipes, touches) SELECT date, count, 0, 0 FROM taps_old"
                )
            } else {
                db.execSQL(
                    "INSERT INTO taps(date, clicks, swipes, touches) SELECT date, clicks, swipes, touches FROM taps_old"
                )
            }
            db.execSQL("DROP TABLE IF EXISTS taps_old")
        }
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS current_session (" +
                "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                "start_time TEXT NOT NULL, " +
                "clicks INTEGER NOT NULL DEFAULT 0, " +
                "swipes INTEGER NOT NULL DEFAULT 0, " +
                "touches INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "INSERT OR IGNORE INTO current_session(id, start_time) VALUES(1, ?)",
            arrayOf(nowStamp())
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "start_time TEXT NOT NULL, " +
                "end_time TEXT NOT NULL, " +
                "clicks INTEGER NOT NULL, " +
                "swipes INTEGER NOT NULL, " +
                "touches INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS app_taps (" +
                "date TEXT NOT NULL, " +
                "package TEXT NOT NULL, " +
                "clicks INTEGER NOT NULL DEFAULT 0, " +
                "swipes INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(date, package))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS screen_stats (" +
                "date TEXT PRIMARY KEY, " +
                "wake_count INTEGER NOT NULL DEFAULT 0, " +
                "active_seconds INTEGER NOT NULL DEFAULT 0)"
        )
    }

    fun onCounted(type: Int) {
        val db = writableDatabase
        val key = todayKey()
        val col = when (type) {
            TYPE_CLICK -> "clicks"
            TYPE_SWIPE -> "swipes"
            else -> "touches"
        }
        db.beginTransaction()
        try {
            var exists = false
            db.rawQuery("SELECT 1 FROM taps WHERE date = ?", arrayOf(key)).use { c ->
                if (c.moveToFirst()) exists = true
            }
            if (exists) {
                db.execSQL("UPDATE taps SET $col = $col + 1 WHERE date = ?", arrayOf(key))
            } else {
                db.execSQL(
                    "INSERT INTO taps(date, clicks, swipes, touches) VALUES(?, ?, ?, ?)",
                    arrayOf(
                        key,
                        if (type == TYPE_CLICK) 1 else 0,
                        if (type == TYPE_SWIPE) 1 else 0,
                        if (type == TYPE_TOUCH) 1 else 0
                    )
                )
            }
            db.execSQL("UPDATE current_session SET $col = $col + 1 WHERE id = 1")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getToday(): DayRecord {
        val db = readableDatabase
        db.rawQuery("SELECT date, clicks, swipes, touches FROM taps WHERE date = ?", arrayOf(todayKey())).use { c ->
            return if (c.moveToFirst()) {
                DayRecord(c.getString(0), c.getLong(1), c.getLong(2), c.getLong(3))
            } else {
                DayRecord(todayKey(), 0, 0, 0)
            }
        }
    }

    fun getDay(date: String): DayRecord? {
        val db = readableDatabase
        db.rawQuery("SELECT date, clicks, swipes, touches FROM taps WHERE date = ?", arrayOf(date)).use { c ->
            return if (c.moveToFirst()) {
                DayRecord(c.getString(0), c.getLong(1), c.getLong(2), c.getLong(3))
            } else {
                null
            }
        }
    }

    fun getCountByDate(date: String, type: Int): Long {
        val col = when (type) {
            TYPE_CLICK -> "clicks"
            TYPE_SWIPE -> "swipes"
            else -> "touches"
        }
        val db = readableDatabase
        db.rawQuery("SELECT $col FROM taps WHERE date = ?", arrayOf(date)).use { c ->
            return if (c.moveToFirst()) c.getLong(0) else 0L
        }
    }

    fun getTotal(): DayRecord {
        val db = readableDatabase
        db.rawQuery(
            "SELECT 'total', COALESCE(SUM(clicks),0), COALESCE(SUM(swipes),0), COALESCE(SUM(touches),0) FROM taps",
            null
        ).use { c ->
            return if (c.moveToFirst()) {
                DayRecord("total", c.getLong(1), c.getLong(2), c.getLong(3))
            } else {
                DayRecord("total", 0, 0, 0)
            }
        }
    }

    fun getHistory(days: Int): List<DayRecord> {
        val result = mutableListOf<DayRecord>()
        val db = readableDatabase
        val start = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(System.currentTimeMillis() - days * 86400000L))
        db.rawQuery(
            "SELECT date, clicks, swipes, touches FROM taps WHERE date >= ? ORDER BY date ASC",
            arrayOf(start)
        ).use { c ->
            while (c.moveToNext()) {
                result.add(DayRecord(c.getString(0), c.getLong(1), c.getLong(2), c.getLong(3)))
            }
        }
        return result
    }

    fun getCurrentSession(): SessionInfo {
        val db = readableDatabase
        db.rawQuery(
            "SELECT start_time, clicks, swipes, touches FROM current_session WHERE id = 1",
            null
        ).use { c ->
            return if (c.moveToFirst()) {
                SessionInfo(c.getString(0), nowStamp(), c.getLong(1), c.getLong(2), c.getLong(3))
            } else {
                SessionInfo(nowStamp(), nowStamp(), 0, 0, 0)
            }
        }
    }

    fun saveCurrentSession() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL(
                "INSERT INTO sessions(start_time, end_time, clicks, swipes, touches) " +
                    "SELECT start_time, ?, clicks, swipes, touches FROM current_session WHERE id = 1",
                arrayOf(nowStamp())
            )
            db.execSQL(
                "UPDATE current_session SET start_time = ?, clicks = 0, swipes = 0, touches = 0 WHERE id = 1",
                arrayOf(nowStamp())
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getSessions(): List<SessionInfo> {
        val result = mutableListOf<SessionInfo>()
        val db = readableDatabase
        db.rawQuery(
            "SELECT start_time, end_time, clicks, swipes, touches FROM sessions ORDER BY id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                result.add(SessionInfo(c.getString(0), c.getString(1), c.getLong(2), c.getLong(3), c.getLong(4)))
            }
        }
        return result
    }

    fun addAppTap(date: String, packageName: String, type: Int) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            var exists = false
            db.rawQuery(
                "SELECT 1 FROM app_taps WHERE date = ? AND package = ?",
                arrayOf(date, packageName)
            ).use { c -> if (c.moveToFirst()) exists = true }
            if (exists) {
                if (type == TYPE_CLICK) {
                    db.execSQL(
                        "UPDATE app_taps SET clicks = clicks + 1 WHERE date = ? AND package = ?",
                        arrayOf(date, packageName)
                    )
                } else {
                    db.execSQL(
                        "UPDATE app_taps SET swipes = swipes + 1 WHERE date = ? AND package = ?",
                        arrayOf(date, packageName)
                    )
                }
            } else {
                db.execSQL(
                    "INSERT INTO app_taps(date, package, clicks, swipes) VALUES(?, ?, ?, ?)",
                    arrayOf(
                        date,
                        packageName,
                        if (type == TYPE_CLICK) 1 else 0,
                        if (type == TYPE_SWIPE) 1 else 0
                    )
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAppRank(date: String): List<AppStat> {
        val result = mutableListOf<AppStat>()
        val db = readableDatabase
        db.rawQuery(
            "SELECT package, clicks, swipes FROM app_taps WHERE date = ? ORDER BY (clicks + swipes) DESC",
            arrayOf(date)
        ).use { c ->
            while (c.moveToNext()) {
                result.add(AppStat(c.getString(0), c.getLong(1), c.getLong(2)))
            }
        }
        return result
    }

    fun getScreenStats(date: String): ScreenStats {
        val db = readableDatabase
        db.rawQuery(
            "SELECT wake_count, active_seconds FROM screen_stats WHERE date = ?",
            arrayOf(date)
        ).use { c ->
            return if (c.moveToFirst()) {
                ScreenStats(c.getLong(0), c.getLong(1))
            } else {
                ScreenStats(0, 0)
            }
        }
    }

    fun addWake(date: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            var exists = false
            db.rawQuery("SELECT 1 FROM screen_stats WHERE date = ?", arrayOf(date)).use { c ->
                if (c.moveToFirst()) exists = true
            }
            if (exists) {
                db.execSQL(
                    "UPDATE screen_stats SET wake_count = wake_count + 1 WHERE date = ?",
                    arrayOf(date)
                )
            } else {
                db.execSQL(
                    "INSERT INTO screen_stats(date, wake_count, active_seconds) VALUES(?, 1, 0)",
                    arrayOf(date)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun addActiveSeconds(date: String, seconds: Int) {
        if (seconds <= 0) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            var exists = false
            db.rawQuery("SELECT 1 FROM screen_stats WHERE date = ?", arrayOf(date)).use { c ->
                if (c.moveToFirst()) exists = true
            }
            if (exists) {
                db.execSQL(
                    "UPDATE screen_stats SET active_seconds = active_seconds + ? WHERE date = ?",
                    arrayOf(seconds, date)
                )
            } else {
                db.execSQL(
                    "INSERT INTO screen_stats(date, wake_count, active_seconds) VALUES(?, 0, ?)",
                    arrayOf(date, seconds)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
