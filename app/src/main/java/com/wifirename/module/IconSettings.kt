package com.wifirename.module

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Launcher-icon choices live in a dedicated preferences file so the mapping
 * preferences (and the Xposed hook) remain untouched.
 */
object IconSettings {

    private const val FILE = "icon_settings"
    private const val KEY_ICON_ID = "icon_id"
    private const val KEY_CUSTOM_BG = "custom_bg"
    private const val KEY_CUSTOM_FG = "custom_fg"

    private val DefaultBg = Color(0xFF1C1670)
    private val DefaultFg = Color(0xFFE37DE6)

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun iconId(context: Context): String {
        val saved = prefs(context).getString(KEY_ICON_ID, IconCatalog.defaultId)
            ?: IconCatalog.defaultId
        return if (IconCatalog.isKnown(saved)) saved else IconCatalog.defaultId
    }

    fun setIconId(context: Context, id: String) {
        if (!IconCatalog.isKnown(id)) return
        prefs(context).edit().putString(KEY_ICON_ID, id).apply()
    }

    fun customBgColor(context: Context): Color =
        Color(prefs(context).getInt(KEY_CUSTOM_BG, DefaultBg.toArgb()))

    fun customFgColor(context: Context): Color =
        Color(prefs(context).getInt(KEY_CUSTOM_FG, DefaultFg.toArgb()))

    fun setCustomColors(context: Context, bg: Color, fg: Color) {
        prefs(context).edit()
            .putInt(KEY_CUSTOM_BG, bg.toArgb())
            .putInt(KEY_CUSTOM_FG, fg.toArgb())
            .apply()
    }
}
