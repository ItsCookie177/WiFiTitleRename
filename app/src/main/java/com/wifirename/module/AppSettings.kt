package com.wifirename.module

import android.content.Context
import org.json.JSONObject

object AppSettings {

    const val SETTINGS_FILE = "app_settings"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    private fun normalizeKey(raw: String): String =
        raw.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim()

    private fun prefs(caller: Context) =
        caller.getSharedPreferences(Prefs.PREFS_NAME, Context.MODE_PRIVATE)

    fun themeMode(context: Context): String =
        context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
            .getString("theme_mode", THEME_SYSTEM) ?: THEME_SYSTEM

    fun setThemeMode(context: Context, mode: String) {
        context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
            .edit().putString("theme_mode", mode).apply()
    }

    fun iconColorMode(context: Context): Int =
        context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
            .getInt("icon_color_mode", 1)

    fun setIconColorMode(context: Context, mode: Int) {
        context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
            .edit().putInt("icon_color_mode", mode).apply()
    }

    fun loadMappings(caller: Context): LinkedHashMap<String, String> {
        val sp = prefs(caller)
        val normalized = LinkedHashMap<String, String>()
        var needsMigration = false
        for ((k, v) in sp.all) {
            val nk = normalizeKey(k)
            if (nk != k) needsMigration = true
            if (nk.isEmpty()) continue
            normalized[nk] = v.toString().trim()
        }
        if (needsMigration) {
            val editor = sp.edit().clear()
            for ((k, v) in normalized) editor.putString(k, v)
            editor.apply()
            Prefs.makeWorldReadable(caller)
        }
        return normalized.entries
            .sortedWith(compareBy({ it.key.lowercase() }, { it.key }))
            .associateTo(LinkedHashMap()) { it.key to it.value }
    }

    fun saveMapping(caller: Context, original: String?, ssid: String, label: String) {
        val key = normalizeKey(ssid)
        val value = label.trim()
        if (key.isEmpty() || value.isEmpty()) return
        val editor = prefs(caller).edit()
        if (original != null) {
            val oldKey = normalizeKey(original)
            if (oldKey != key) editor.remove(oldKey)
        }
        editor.putString(key, value)
        editor.apply()
        Prefs.makeWorldReadable(caller)
    }

    fun deleteMapping(caller: Context, ssid: String) {
        prefs(caller).edit().remove(normalizeKey(ssid)).apply()
        Prefs.makeWorldReadable(caller)
    }

    fun deleteMappings(caller: Context, ssids: Collection<String>) {
        if (ssids.isEmpty()) return
        val editor = prefs(caller).edit()
        for (ssid in ssids) editor.remove(normalizeKey(ssid))
        editor.apply()
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
        if (text.isBlank()) return null
        return try {
            val root = JSONObject(text)
            val m = root.optJSONObject("mappings") ?: return null
            val out = LinkedHashMap<String, String>()
            for (key in m.keys()) {
                val nk = normalizeKey(key)
                val label = m.optString(key).trim()
                if (nk.isNotEmpty() && label.isNotEmpty()) out[nk] = label
            }
            out
        } catch (_: Exception) {
            null
        }
    }

    fun mergeMappings(caller: Context, imported: Map<String, String>) {
        val editor = prefs(caller).edit()
        for ((k, v) in imported) editor.putString(k, v)
        editor.apply()
        Prefs.makeWorldReadable(caller)
    }

    fun replaceMappings(caller: Context, imported: Map<String, String>) {
        val editor = prefs(caller).edit()
        editor.clear()
        for ((k, v) in imported) editor.putString(k, v)
        editor.apply()
        Prefs.makeWorldReadable(caller)
    }

    fun clearAll(caller: Context) {
        prefs(caller).edit().clear().apply()
        Prefs.makeWorldReadable(caller)
    }
}
