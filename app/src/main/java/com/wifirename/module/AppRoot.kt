package com.wifirename.module

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val LOG_TAG = "WifiRenameUI"

enum class Tabs { MAPPING, SETTINGS }
enum class RootPage { MAIN, ABOUT }

data class Mapping(val ssid: String, val label: String)

@Composable
fun AppRoot(
    mappings: List<Mapping>,
    versionText: String,
    themeMode: String,
    dark: Boolean,
    onThemeChanged: (String) -> Unit,
    onSave: (original: String?, ssid: String, label: String) -> Unit,
    onDelete: (String) -> Unit,
    onDeleteMany: (Collection<String>) -> Unit,
    onClearAll: () -> Unit,
    onExportPick: (Uri?) -> Unit,
    onImportPick: (Uri?, Boolean) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(Tabs.MAPPING) }
    var page by rememberSaveable { mutableStateOf(RootPage.MAIN) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val mappingScrollBehavior = MiuixScrollBehavior()
    val settingsScrollBehavior = MiuixScrollBehavior()
    var infoDismissed by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var selecting by rememberSaveable { mutableStateOf(false) }
    val selectedSsids = remember { mutableStateListOf<String>() }

    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingOriginal by rememberSaveable { mutableStateOf<String?>(null) }
    var ssidInput by rememberSaveable { mutableStateOf("") }
    var labelInput by rememberSaveable { mutableStateOf("") }
    var deleteTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteSelectedAsk by rememberSaveable { mutableStateOf(false) }
    var showImportAsk by rememberSaveable { mutableStateOf(false) }
    var pendingReplace by rememberSaveable { mutableStateOf(false) }
    var showClearAllAsk by rememberSaveable { mutableStateOf(false) }
    var showThemeAsk by rememberSaveable { mutableStateOf(false) }

    fun exitSelection() {
        selecting = false
        selectedSsids.clear()
    }

    fun openEditor(m: Mapping?) {
        editingOriginal = m?.ssid
        ssidInput = m?.ssid ?: ""
        labelInput = m?.label ?: ""
        showEditor = true
    }

    val visibleMappings = filterMappings(mappings, searchQuery)

    BackHandler(enabled = selecting) {
        exitSelection()
    }

    val exportPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        Log.d(LOG_TAG, "export picked file=${uri != null}")
        onExportPick(uri)
    }

    val importPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        Log.d(LOG_TAG, "import picked file=${uri != null} replace=$pendingReplace")
        onImportPick(uri, pendingReplace)
    }

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            if (targetState == RootPage.ABOUT) {
                (slideInVertically(tween(250)) { it / 6 } + fadeIn(tween(250))) togetherWith
                    (slideOutHorizontally(tween(250)) { -it / 8 } + fadeOut(tween(200)))
            } else {
                (slideInHorizontally(tween(250)) { -it / 8 } + fadeIn(tween(250))) togetherWith
                    (slideOutHorizontally(tween(250)) { it / 6 } + fadeOut(tween(200)))
            }
        },
        label = "root",
    ) { current ->
        if (current == RootPage.ABOUT) {
            AboutScreen(
                versionText = versionText,
                dark = dark,
                onBack = { page = RootPage.MAIN },
            )
        } else {
            Scaffold(
                topBar = {
                    AnimatedContent(
                        targetState = tab,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                        label = "topbar",
                    ) { t ->
                        if (t == Tabs.MAPPING) {
                            if (selecting) {
                                SmallTopAppBar(
                                    title = "${selectedSsids.size} dipilih",
                                    navigationIcon = {
                                        IconButton(onClick = { exitSelection() }) {
                                            Icon(
                                                imageVector = MiuixIcons.Close,
                                                contentDescription = "Keluar dari mode pilih",
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = {
                                                val all = visibleMappings.map { it.ssid }
                                                val allSelected = all.isNotEmpty() && all.all { it in selectedSsids }
                                                if (allSelected) {
                                                    selectedSsids.removeAll(all.toSet())
                                                } else {
                                                    all.forEach { if (it !in selectedSsids) selectedSsids.add(it) }
                                                }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.SelectAll,
                                                contentDescription = "Pilih semua",
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (selectedSsids.isNotEmpty()) showDeleteSelectedAsk = true
                                            },
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Delete,
                                                contentDescription = "Hapus terpilih",
                                            )
                                        }
                                    },
                                )
                            } else {
                                TopAppBar(
                                    title = "WiFi Title Rename",
                                    largeTitle = "WiFi Title Rename",
                                    scrollBehavior = mappingScrollBehavior,
                                    actions = {
                                        IconButton(onClick = { openEditor(null) }) {
                                            Icon(
                                                imageVector = MiuixIcons.Add,
                                                contentDescription = "Tambah mapping",
                                            )
                                        }
                                    },
                                )
                            }
                        } else {
                            TopAppBar(
                                title = "Pengaturan",
                                largeTitle = "Pengaturan",
                                scrollBehavior = settingsScrollBehavior,
                            )
                        }
                    }
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = tab == Tabs.MAPPING,
                            onClick = {
                                Log.d(LOG_TAG, "tab -> mapping")
                                tab = Tabs.MAPPING
                            },
                            icon = MiuixIcons.ListView,
                            label = "Mapping",
                        )
                        NavigationBarItem(
                            selected = tab == Tabs.SETTINGS,
                            onClick = {
                                Log.d(LOG_TAG, "tab -> settings")
                                exitSelection()
                                tab = Tabs.SETTINGS
                            },
                            icon = MiuixIcons.Settings,
                            label = "Pengaturan",
                        )
                    }
                },
            ) { paddingValues ->
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        val dir = if (targetState == Tabs.SETTINGS) 1 else -1
                        (slideInHorizontally(tween(220)) { dir * it / 5 } + fadeIn(tween(220))) togetherWith
                            fadeOut(tween(220))
                    },
                    label = "tabs",
                ) { t ->
                    if (t == Tabs.MAPPING) {
                        MappingContent(
                            mappings = mappings,
                            visibleMappings = visibleMappings,
                            listState = listState,
                            scrollBehavior = mappingScrollBehavior,
                            paddingValues = paddingValues,
                            infoDismissed = infoDismissed,
                            onInfoDismiss = {
                                Log.d(LOG_TAG, "info card dismissed")
                                infoDismissed = true
                            },
                            searchQuery = searchQuery,
                            onSearchChanged = { searchQuery = it },
                            selecting = selecting,
                            selectedSsids = selectedSsids,
                            onRowClick = { m ->
                                if (selecting) {
                                    if (m.ssid in selectedSsids) selectedSsids.remove(m.ssid)
                                    else selectedSsids.add(m.ssid)
                                } else {
                                    openEditor(m)
                                }
                            },
                            onRowLongClick = { m ->
                                selecting = true
                                if (m.ssid !in selectedSsids) selectedSsids.add(m.ssid)
                            },
                            onAddClick = { openEditor(null) },
                        )
                    } else {
                        SettingsContent(
                            versionText = versionText,
                            themeMode = themeMode,
                            scrollBehavior = settingsScrollBehavior,
                            paddingValues = paddingValues,
                            onThemeClick = {
                                Log.d(LOG_TAG, "theme dialog open")
                                showThemeAsk = true
                            },
                            onExport = {
                                Log.d(LOG_TAG, "export mapping")
                                exportPicker.launch("wifirename-mappings.json")
                            },
                            onImport = {
                                Log.d(LOG_TAG, "import mapping")
                                showImportAsk = true
                            },
                            onClearAll = {
                                Log.d(LOG_TAG, "clear-all mapping")
                                showClearAllAsk = true
                            },
                            onOpenAbout = {
                                Log.d(LOG_TAG, "about open")
                                page = RootPage.ABOUT
                            },
                        )
                    }
                }

                // ---- dialogs: composed INSIDE Scaffold scope so they register
                //      into the Scaffold popupHost (see fix for dead taps) ----
                if (showEditor) {
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
                                    Log.d(
                                        LOG_TAG,
                                        "save clicked original=$editingOriginal ssid=${ssidInput.trim()}",
                                    )
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
                }

                if (deleteTarget != null) {
                    OverlayDialog(
                        show = deleteTarget != null,
                        title = "Hapus mapping?",
                        summary = deleteTarget,
                        onDismissRequest = { deleteTarget = null },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(
                                text = "Hapus",
                                onClick = {
                                    Log.d(LOG_TAG, "confirm delete ${deleteTarget}")
                                    deleteTarget?.let { onDelete(it) }
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

                if (showDeleteSelectedAsk) {
                    val count = selectedSsids.size
                    OverlayDialog(
                        show = showDeleteSelectedAsk,
                        title = "Hapus $count mapping?",
                        summary = "Mapping yang dipilih akan dihapus permanen.",
                        onDismissRequest = { showDeleteSelectedAsk = false },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(
                                text = "Hapus",
                                onClick = {
                                    Log.d(LOG_TAG, "confirm delete-selected count=$count")
                                    onDeleteMany(selectedSsids.toList())
                                    showDeleteSelectedAsk = false
                                    exitSelection()
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                text = "Batal",
                                onClick = { showDeleteSelectedAsk = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (showImportAsk) {
                    OverlayDialog(
                        show = showImportAsk,
                        title = "Impor mapping",
                        summary = "Pilih cara menggabungkan mapping dari file JSON.",
                        onDismissRequest = { showImportAsk = false },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(
                                text = "Gabung",
                                onClick = {
                                    Log.d(LOG_TAG, "import mode=merge")
                                    pendingReplace = false
                                    showImportAsk = false
                                    importPicker.launch(arrayOf("application/json"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                text = "Timpa",
                                onClick = {
                                    Log.d(LOG_TAG, "import mode=replace")
                                    pendingReplace = true
                                    showImportAsk = false
                                    importPicker.launch(arrayOf("application/json"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                text = "Batal",
                                onClick = { showImportAsk = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (showClearAllAsk) {
                    OverlayDialog(
                        show = showClearAllAsk,
                        title = "Hapus semua mapping?",
                        summary = "Semua mapping akan dihapus permanen.",
                        onDismissRequest = { showClearAllAsk = false },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(
                                text = "Hapus semua",
                                onClick = {
                                    Log.d(LOG_TAG, "confirm clear-all")
                                    onClearAll()
                                    showClearAllAsk = false
                                    exitSelection()
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                text = "Batal",
                                onClick = { showClearAllAsk = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (showThemeAsk) {
                    OverlayDialog(
                        show = showThemeAsk,
                        title = "Tema",
                        onDismissRequest = { showThemeAsk = false },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ThemeOption(
                                text = "Ikut sistem",
                                selected = themeMode == AppSettings.THEME_SYSTEM,
                                onClick = {
                                    Log.d(LOG_TAG, "theme -> system")
                                    onThemeChanged(AppSettings.THEME_SYSTEM)
                                    showThemeAsk = false
                                },
                            )
                            ThemeOption(
                                text = "Terang",
                                selected = themeMode == AppSettings.THEME_LIGHT,
                                onClick = {
                                    Log.d(LOG_TAG, "theme -> light")
                                    onThemeChanged(AppSettings.THEME_LIGHT)
                                    showThemeAsk = false
                                },
                            )
                            ThemeOption(
                                text = "Gelap",
                                selected = themeMode == AppSettings.THEME_DARK,
                                onClick = {
                                    Log.d(LOG_TAG, "theme -> dark")
                                    onThemeChanged(AppSettings.THEME_DARK)
                                    showThemeAsk = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
