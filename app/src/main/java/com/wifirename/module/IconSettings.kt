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
    private const val KEY_CUSTOM_TEMPLATE = "custom_template"

    val defaultCustomBg: Color = Color(0xFF1C1670)
    val defaultCustomFg: Color = Color(0xFFE37DE6)

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun iconId(context: Context): String {
        val saved = prefs(context).getString(KEY_ICON_ID, IconCatalog.defaultId)
            ?: IconCatalog.defaultId
        return if (IconCatalog.isKnown(saved)) saved else IconCatalog.defaultId
    }

    fun setIconId(context: Context, id: String): Boolean {
        if (!IconCatalog.isKnown(id)) return false
        return prefs(context).edit().putString(KEY_ICON_ID, id).commit()
    }

    fun customBgColor(context: Context): Color =
        Color(prefs(context).getInt(KEY_CUSTOM_BG, defaultCustomBg.toArgb())).copy(alpha = 1f)

    fun customFgColor(context: Context): Color =
        Color(prefs(context).getInt(KEY_CUSTOM_FG, defaultCustomFg.toArgb()))

    fun setCustomColors(context: Context, bg: Color, fg: Color) {
        prefs(context).edit()
            .putInt(KEY_CUSTOM_BG, bg.copy(alpha = 1f).toArgb())
            .putInt(KEY_CUSTOM_FG, fg.toArgb())
            .apply()
    }

    fun customTemplateSelected(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CUSTOM_TEMPLATE, false)

    fun setCustomTemplateSelected(context: Context, selected: Boolean) {
        prefs(context).edit().putBoolean(KEY_CUSTOM_TEMPLATE, selected).apply()
    }
}
