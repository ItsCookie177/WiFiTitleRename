package com.wifirename.module

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiuixThemeWrapper {
                val mappings = remember {
                    mutableStateListOf<Mapping>().apply { addAll(loadMappings()) }
                }
                MainScreen(
                    mappings = mappings,
                    onSave = { original, ssid, label -> saveMapping(mappings, original, ssid, label) },
                    onDelete = { ssid -> deleteMapping(mappings, ssid) },
                )
            }
        }
    }

    private fun loadMappings(): List<Mapping> {
        val all = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE).all
        return all.entries.map { Mapping(it.key, it.value.toString()) }.sortedBy { it.ssid }
    }

    private fun saveMapping(
        mappings: MutableList<Mapping>,
        original: String?,
        ssid: String,
        label: String,
    ) {
        val editor = getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE).edit()
        if (original != null && original != ssid) {
            editor.remove(original)
        }
        editor.putString(ssid, label)
        editor.apply()
        Prefs.makeWorldReadable(this)
        mappings.removeAll { it.ssid == ssid || (original != null && it.ssid == original) }
        mappings.add(Mapping(ssid, label))
        mappings.sortBy { it.ssid }
    }

    private fun deleteMapping(mappings: MutableList<Mapping>, ssid: String) {
        getSharedPreferences(Prefs.PREFS_NAME, MODE_PRIVATE).edit().remove(ssid).apply()
        Prefs.makeWorldReadable(this)
        mappings.removeAll { it.ssid == ssid }
    }
}
