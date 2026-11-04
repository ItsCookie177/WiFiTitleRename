package com.wifirename.module

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
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
                val mappings = remember { mutableStateListOf<Mapping>() }

                fun reloadMappings() {
                    val loaded = AppSettings.loadMappings(this@MainActivity)
                    mappings.clear()
                    mappings.addAll(loaded.map { Mapping(it.key, it.value) })
                }

                LaunchedEffect(Unit) { reloadMappings() }

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
                        reloadMappings()
                    },
                    onDelete = { ssid ->
                        AppSettings.deleteMapping(this, ssid)
                        reloadMappings()
                    },
                    onDeleteMany = { ssids ->
                        AppSettings.deleteMappings(this, ssids)
                        reloadMappings()
                    },
                    onClearAll = {
                        AppSettings.clearAll(this)
                        reloadMappings()
                    },
                    onExportPick = { uri ->
                        val count = exportMappings(uri)
                        val msg = if (count == null) {
                            getString(R.string.toast_export_failed)
                        } else {
                            getString(R.string.toast_export_ok, count)
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    },
                    onImportPick = { uri, replace ->
                        val count = importMappings(uri, replace)
                        if (count == null) {
                            Toast.makeText(
                                this,
                                getString(R.string.toast_import_invalid),
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            reloadMappings()
                            Toast.makeText(
                                this,
                                getString(R.string.toast_import_ok, count),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }
        }
    }

    private fun versionText(): String {
        return try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            "v${pi.versionName ?: "1.4.0"} (${pi.longVersionCode and 0x7FFFFFFF})"
        } catch (_: Exception) {
            "v1.4.0 (7)"
        }
    }

    private fun exportMappings(uri: Uri?): Int? {
        if (uri == null) return null
        return try {
            val mappings = AppSettings.loadMappings(this)
            contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os, Charsets.UTF_8).use { w ->
                    w.write(AppSettings.exportJson(this))
                    w.flush()
                }
            } ?: return null
            mappings.size
        } catch (_: Exception) {
            null
        }
    }

    private fun importMappings(uri: Uri?, replace: Boolean): Int? {
        if (uri == null) return null
        return try {
            val text = contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: return null
            val parsed = AppSettings.parseImport(this, text)
            if (parsed.isNullOrEmpty()) return null
            if (replace) AppSettings.replaceMappings(this, parsed) else AppSettings.mergeMappings(this, parsed)
            parsed.size
        } catch (_: Exception) {
            null
        }
    }
}
