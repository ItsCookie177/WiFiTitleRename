package com.wifirename.module

import android.content.Context
import org.json.JSONObject

object AppSettings {

    const val SETTINGS_FILE = "app_settings"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    fun themeMode(context: Context): String =
        context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
            .getString("theme_mode", THEME_SYSTEM) ?: THEME_SYSTEM

    fun setThemeMode(context: Context, mode: String) {
        context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
            .edit().putString("theme_mode", mode).apply()
    }

    fun loadMappings(caller: Context): LinkedHashMap<String, String> {
        val sp = caller.getSharedPreferences(Prefs.PREFS_NAME, Context.MODE_PRIVATE)
        val out = LinkedHashMap<String, String>()
        for ((k, v) in sp.all) out[k] = v.toString()
        return out
    }

    fun saveMapping(caller: Context, original: String?, ssid: String, label: String) {
        val editor = caller.getSharedPreferences(Prefs.PREFS_NAME, Context.MODE_PRIVATE).edit()
        if (original != null && original != ssid) {
            editor.remove(original)
        }
        editor.putString(ssid, label)
        editor.apply()
        Prefs.makeWorldReadable(caller)
    }

    fun deleteMapping(caller: Context, ssid: String) {
        caller.getSharedPreferences(Prefs.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(ssid).apply()
        Prefs.makeWorldReadable(caller)
    }

    fun exportJson(caller: Context): String {
        val o = JSONObject()
        o.put("version", 1)
        val m = JSONObject()
        for ((k, v) in loadMappings(caller)) m.put(k, v)
        o.put("mappings", m)
        return o.toString(2)
    }

    fun parseImport(caller: Context, text: String): LinkedHashMap<String, String>? {
        return try {
            val root = JSONObject(text)
            val m = root.optJSONObject("mappings") ?: JSONObject()
            val out = LinkedHashMap<String, String>()
            for (key in m.keys()) {
                val label = m.optString(key)
                if (!label.isNullOrEmpty()) out[key] = label
            }
            out
        } catch (_: Exception) {
            null
        }
    }

    fun mergeMappings(caller: Context, imported: Map<String, String>) {
        val editor = caller.getSharedPreferences(Prefs.PREFS_NAME, Context.MODE_PRIVATE).edit()
        for ((k, v) in imported) editor.putString(k, v)
        editor.apply()
        Prefs.makeWorldReadable(caller)
    }

    fun replaceMappings(caller: Context, imported: Map<String, String>) {
        val editor = caller.getSharedPreferences(Prefs.PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.clear()
        for ((k, v) in imported) editor.putString(k, v)
        editor.apply()
        Prefs.makeWorldReadable(caller)
    }

    fun clearAll(caller: Context) {
        caller.getSharedPreferences(Prefs.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
        Prefs.makeWorldReadable(caller)
    }
}
