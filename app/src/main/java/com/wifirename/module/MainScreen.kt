package com.wifirename.module

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

data class Mapping(val ssid: String, val label: String)

@Composable
fun MiuixThemeWrapper(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MiuixTheme(colors = colors, content = content)
}

@Composable
fun MainScreen(
    mappings: List<Mapping>,
    onSave: (original: String?, ssid: String, label: String) -> Unit,
    onDelete: (ssid: String) -> Unit,
) {
    var showEditor by remember { mutableStateOf(false) }
    var editingOriginal by remember { mutableStateOf<String?>(null) }
    var ssidInput by remember { mutableStateOf("") }
    var labelInput by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Mapping?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = stringResource(R.string.app_name))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingOriginal = null
                ssidInput = ""
                labelInput = ""
                showEditor = true
            }) {
                Text(
                    text = "+",
                    color = MiuixTheme.colorScheme.onPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(insideMargin = PaddingValues(14.dp, 12.dp)) {
                    Text(
                        text = "Aktifkan modul di Vector, centang scope Settings dan System UI, lalu restart System UI.",
                        fontSize = 13.sp,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                }
            }
            if (mappings.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada mapping.",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 48.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                }
            } else {
                items(mappings, key = { it.ssid }) { m ->
                    MappingCard(
                        mapping = m,
                        onClick = {
                            editingOriginal = m.ssid
                            ssidInput = m.ssid
                            labelInput = m.label
                            showEditor = true
                        },
                        onDelete = { deleteTarget = m },
                    )
                }
            }
        }
    }

    OverlayDialog(
        show = showEditor,
        title = if (editingOriginal == null) "Tambah mapping" else "Edit mapping",
        onDismissRequest = { showEditor = false },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TextField(
                value = ssidInput,
                onValueChange = { ssidInput = it },
                label = "SSID asli (persis seperti yang tampil)",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            TextField(
                value = labelInput,
                onValueChange = { labelInput = it },
                label = "Nama tampilan baru",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            val invalid = ssidInput.isBlank() || labelInput.isBlank()
            if (invalid) {
                Text(
                    text = "Keduanya tidak boleh kosong",
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            TextButton(
                text = "Simpan",
                onClick = {
                    onSave(editingOriginal, ssidInput.trim(), labelInput.trim())
                    showEditor = false
                },
                enabled = !invalid,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                text = "Batal",
                onClick = { showEditor = false },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    OverlayDialog(
        show = deleteTarget != null,
        title = "Hapus mapping?",
        summary = deleteTarget?.ssid,
        onDismissRequest = { deleteTarget = null },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(
                text = "Hapus",
                onClick = {
                    deleteTarget?.let { onDelete(it.ssid) }
                    deleteTarget = null
                },
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                text = "Batal",
                onClick = { deleteTarget = null },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MappingCard(
    mapping: Mapping,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        insideMargin = PaddingValues(14.dp, 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = mapping.ssid,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = MiuixIcons.Basic.Close,
                    contentDescription = "Hapus",
                    tint = MiuixTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier
                    .size(12.dp, 16.dp)
                    .rotate(90f),
            )
        }
        Text(
            text = mapping.label,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
