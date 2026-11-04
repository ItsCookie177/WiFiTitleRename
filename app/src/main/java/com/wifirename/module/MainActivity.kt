package com.wifirename.module

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import java.io.OutputStreamWriter

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(AppSettings.themeMode(this)) }
            val systemDark = isSystemInDarkTheme()
            val dark = when (themeMode) {
                AppSettings.THEME_LIGHT -> false
                AppSettings.THEME_DARK -> true
                else -> systemDark
            }
            val controller = remember(themeMode, systemDark) {
                ThemeController(
                    colorSchemeMode = when (themeMode) {
                        AppSettings.THEME_LIGHT -> ColorSchemeMode.Light
                        AppSettings.THEME_DARK -> ColorSchemeMode.Dark
                        else -> ColorSchemeMode.System
                    },
                    lightColors = lightColorScheme(),
                    darkColors = darkColorScheme(),
                    isDark = systemDark,
                )
            }
            val view = LocalView.current
            SideEffect {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            MiuixTheme(controller = controller) {
                val mappings = remember {
                    mutableStateListOf<Mapping>().apply { addAll(AppSettings.loadMappings(this@MainActivity).map { Mapping(it.key, it.value) }) }
                }
                AppRoot(
                    mappings = mappings,
                    versionText = versionText(),
                    themeMode = themeMode,
                    dark = dark,
                    onThemeChanged = { mode ->
                        themeMode = mode
                        AppSettings.setThemeMode(this, mode)
                    },
                    onSave = { original, ssid, label ->
                        AppSettings.saveMapping(this, original, ssid, label)
                        mappings.removeAll { it.ssid == ssid || (original != null && it.ssid == original) }
                        mappings.add(Mapping(ssid, label))
                        mappings.sortBy { it.ssid }
                    },
                    onDelete = { ssid ->
                        AppSettings.deleteMapping(this, ssid)
                        mappings.removeAll { it.ssid == ssid }
                    },
                    onDeleteMany = { ssids ->
                        AppSettings.deleteMappings(this, ssids)
                        mappings.removeAll { it.ssid in ssids }
                    },
                    onClearAll = {
                        AppSettings.clearAll(this)
                        mappings.clear()
                    },
                    onExportPick = { uri -> exportMappings(uri) },
                    onImportPick = { uri, replace -> importMappings(uri, replace) },
                )
            }
        }
    }

    private fun versionText(): String {
        return try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            "v${pi.versionName ?: "1.3.0"} (${pi.longVersionCode and 0x7FFFFFFF})"
        } catch (_: Exception) {
            "v1.3.0 (6)"
        }
    }

    private fun exportMappings(uri: Uri?) {
        if (uri == null) return
        try {
            contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os, Charsets.UTF_8).use { w ->
                    w.write(AppSettings.exportJson(this))
                    w.flush()
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun importMappings(uri: Uri?, replace: Boolean) {
        if (uri == null) return
        try {
            val text = contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: return
            val parsed = AppSettings.parseImport(this, text) ?: return
            if (parsed.isEmpty()) return
            if (replace) AppSettings.replaceMappings(this, parsed) else AppSettings.mergeMappings(this, parsed)
        } catch (_: Exception) {
        }
    }
}
