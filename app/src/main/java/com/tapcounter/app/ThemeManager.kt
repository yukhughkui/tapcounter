package com.tapcounter.app

import android.content.Context
import android.util.TypedValue

object ThemeManager {

    const val BUSINESS = "business"
    const val KITTY = "kitty"
    const val GENSHIN = "genshin"
    const val PONY = "pony"

    private const val PREFS = "tap_counter_theme"
    private const val KEY = "theme"

    fun current(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, BUSINESS) ?: BUSINESS

    fun save(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, name).apply()
    }

    fun styleOf(name: String): Int = when (name) {
        KITTY -> R.style.Theme_TapCounter_Kitty
        GENSHIN -> R.style.Theme_TapCounter_Genshin
        PONY -> R.style.Theme_TapCounter_Pony
        else -> R.style.Theme_TapCounter
    }

    fun color(context: Context, attrRes: Int): Int {
        val tv = TypedValue()
        if (!context.theme.resolveAttribute(attrRes, tv, true)) return 0
        return tv.data
    }
}
