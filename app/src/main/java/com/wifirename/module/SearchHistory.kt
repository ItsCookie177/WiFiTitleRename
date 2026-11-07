package com.wifirename.module

import android.content.Context
import org.json.JSONArray

object SearchHistory {

    private const val FILE = "search_history"
    private const val KEY_ITEMS = "items"
    private const val MAX_ITEMS = 10

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun load(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val items = ArrayList<String>()
            for (index in 0 until array.length()) {
                val item = array.optString(index).trim()
                if (item.isNotEmpty() && items.none { it.equals(item, ignoreCase = true) }) {
                    items.add(item)
                }
            }
            items.take(MAX_ITEMS)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, query: String) {
        val item = query.trim()
        if (item.isEmpty()) return
        val updated = ArrayList<String>()
        updated.add(item)
        for (entry in load(context)) {
            if (!entry.equals(item, ignoreCase = true)) updated.add(entry)
        }
        val array = JSONArray()
        for (entry in updated.take(MAX_ITEMS)) array.put(entry)
        prefs(context).edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_ITEMS).apply()
    }
}
