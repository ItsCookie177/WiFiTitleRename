package com.wifirename.module

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
fun SettingsContent(
    versionText: String,
    themeMode: String,
    onThemeClick: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearAll: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val themeLabel = when (themeMode) {
        AppSettings.THEME_LIGHT -> "Terang"
        AppSettings.THEME_DARK -> "Gelap"
        else -> "Ikut sistem"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
    ) {
        item(key = "g1-title") {
            SmallTitle(text = "Tampilan")
        }
        item(key = "g1-card") {
            Card(insideMargin = PaddingValues(14.dp, 6.dp)) {
                ArrowPreference(
                    title = "Tema",
                    summary = themeLabel,
                    onClick = onThemeClick,
                )
            }
        }

        item(key = "g2-title") {
            SmallTitle(text = "Backup & restore")
        }
        item(key = "g2-card") {
            Card(insideMargin = PaddingValues(14.dp, 6.dp)) {
                ArrowPreference(
                    title = "Ekspor mapping",
                    summary = "Simpan semua mapping ke file JSON",
                    onClick = onExport,
                )
                ArrowPreference(
                    title = "Impor mapping",
                    summary = "Muat mapping dari file JSON",
                    onClick = onImport,
                )
            }
        }

        item(key = "g3-title") {
            SmallTitle(text = "Lainnya")
        }
        item(key = "g3-card") {
            Card(insideMargin = PaddingValues(14.dp, 6.dp)) {
                ArrowPreference(
                    title = "Hapus semua mapping",
                    summary = "Tindakan ini tidak dapat dibatalkan",
                    onClick = onClearAll,
                )
                ArrowPreference(
                    title = "About",
                    onClick = onOpenAbout,
                    endActions = { Text(text = versionText) },
                )
            }
        }
    }
}
