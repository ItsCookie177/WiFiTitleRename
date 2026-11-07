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
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optString(index).trim()
                    if (item.isNotEmpty() && none { it.equals(item, ignoreCase = true) }) {
                        add(item)
                    }
                }
            }.take(MAX_ITEMS)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, query: String) {
        val item = query.trim()
        if (item.isEmpty()) return
        val updated = buildList {
            add(item)
            addAll(load(context).filterNot { it.equals(item, ignoreCase = true) })
        }.take(MAX_ITEMS)
        val array = JSONArray()
        for (entry in updated) array.put(entry)
        prefs(context).edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_ITEMS).apply()
    }
}
