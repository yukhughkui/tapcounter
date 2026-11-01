package com.tapcounter.app

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.View
import java.io.File

object WallpaperManager {

    const val KIND_MAIN = "main"
    const val KIND_SUB = "sub"

    private fun file(context: Context, kind: String): File {
        val theme = ThemeManager.current(context)
        val d = File(File(context.filesDir, "wallpapers"), theme)
        d.mkdirs()
        return File(d, "$kind.jpg")
    }

    fun has(context: Context, kind: String): Boolean = file(context, kind).exists()

    fun delete(context: Context, kind: String): Boolean = file(context, kind).delete()

    fun save(context: Context, uri: Uri, kind: String): Boolean {
        return try {
            val target = file(context, kind)
            val ok = context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { out -> input.copyTo(out) }
            } != null
            if (ok) target
            ok
        } catch (e: Exception) {
            false
        }
    }

    fun coverDrawable(context: Context, kind: String): BitmapDrawable? {
        val f = file(context, kind)
        if (!f.exists()) return null
        val bmp = BitmapFactory.decodeFile(f.absolutePath) ?: return null
        val dm = context.resources.displayMetrics
        val scale = maxOf(
            dm.widthPixels.toFloat() / bmp.width,
            dm.heightPixels.toFloat() / bmp.height
        )
        val scaled = Bitmap.createScaledBitmap(
            bmp,
            (bmp.width * scale).toInt(),
            (bmp.height * scale).toInt(),
            true
        )
        return BitmapDrawable(context.resources, scaled)
    }

    fun applyWindow(activity: Activity, kind: String) {
        val d = coverDrawable(activity, kind) ?: return
        activity.window.setBackgroundDrawable(d)
    }

    fun applyToRoot(activity: Activity, kind: String) {
        val d = coverDrawable(activity, kind) ?: return
        val root = activity.findViewById<View>(R.id.bgRoot) ?: return
        root.background = d
    }
}
