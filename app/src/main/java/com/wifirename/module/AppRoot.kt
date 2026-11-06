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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextButtonColors
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
enum class RootPage { MAIN, ABOUT, ICONS }

data class Mapping(val ssid: String, val label: String)

@Composable
fun dangerTextButtonColors(): TextButtonColors = ButtonDefaults.textButtonColors(
    color = MiuixTheme.colorScheme.error,
    textColor = MiuixTheme.colorScheme.onError,
    disabledColor = MiuixTheme.colorScheme.errorContainer,
    disabledTextColor = MiuixTheme.colorScheme.onErrorContainer,
)

@Composable
fun AppRoot(
    mappings: List<Mapping>,
    versionText: String,
    themeMode: String,
    dark: Boolean,
    startOnIcons: Boolean,
    activeIconId: String,
    onThemeChanged: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onSave: (original: String?, ssid: String, label: String) -> Unit,
    onDelete: (String) -> Unit,
    onDeleteMany: (Collection<String>) -> Unit,
    onClearAll: () -> Unit,
    onExportPick: (Uri?) -> Unit,
    onImportPick: (Uri?, Boolean) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(Tabs.MAPPING) }
    var page by rememberSaveable {
        mutableStateOf(if (startOnIcons) RootPage.ICONS else RootPage.MAIN)
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val mappingScrollBehavior = MiuixScrollBehavior()
    val settingsScrollBehavior = MiuixScrollBehavior()
    var infoDismissed by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var selecting by rememberSaveable { mutableStateOf(false) }
    var selectedSsids by rememberSaveable(
        stateSaver = Saver<Set<String>, List<String>>(
            save = { it.toList() },
            restore = { it.toSet() },
        ),
    ) { mutableStateOf(emptySet<String>()) }

    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingOriginal by rememberSaveable { mutableStateOf<String?>(null) }
    var ssidInput by rememberSaveable { mutableStateOf("") }
    var labelInput by rememberSaveable { mutableStateOf("") }
    var deleteTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteSelectedAsk by rememberSaveable { mutableStateOf(false) }
    var showImportAsk by rememberSaveable { mutableStateOf(false) }
    var pendingReplace by rememberSaveable { mutableStateOf(false) }
    var showClearAllAsk by rememberSaveable { mutableStateOf(false) }

    fun exitSelection() {
        selecting = false
        selectedSsids = emptySet()
    }

    fun openEditor(m: Mapping?) {
        editingOriginal = m?.ssid
        ssidInput = m?.ssid ?: ""
        labelInput = m?.label ?: ""
        showEditor = true
    }

    val visibleMappings = filterMappings(mappings, searchQuery)

    LaunchedEffect(tab) {
        if (selecting) exitSelection()
    }
    LaunchedEffect(mappings.size, selectedSsids.size) {
        if (selecting) {
            selectedSsids = selectedSsids.intersect(mappings.map { it.ssid }.toSet())
            if (mappings.isEmpty()) exitSelection()
        }
    }

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
            when (targetState) {
                RootPage.ABOUT -> {
                    (slideInVertically(tween(250)) { it / 6 } + fadeIn(tween(250))) togetherWith
                        (slideOutHorizontally(tween(250)) { -it / 8 } + fadeOut(tween(200)))
                }

                RootPage.ICONS -> {
                    (slideInHorizontally(tween(250)) { it / 5 } + fadeIn(tween(250))) togetherWith
                        (slideOutHorizontally(tween(250)) { -it / 8 } + fadeOut(tween(200)))
                }

                else -> {
                    (slideInHorizontally(tween(250)) { -it / 8 } + fadeIn(tween(250))) togetherWith
                        (slideOutHorizontally(tween(250)) { it / 6 } + fadeOut(tween(200)))
                }
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
        } else if (current == RootPage.ICONS) {
            IconsColorsScreen(
                activeIconId = activeIconId,
                onIconSelected = onIconSelected,
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
                                    title = stringResource(R.string.selected_count, selectedSsids.size),
                                    navigationIcon = {
                                        IconButton(
                                            onClick = { exitSelection() },
                                            minWidth = 48.dp,
                                            minHeight = 48.dp,
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Close,
                                                contentDescription = stringResource(R.string.action_exit_selection),
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = {
                                                val all = visibleMappings.map { it.ssid }
                                                val allSelected = all.isNotEmpty() && all.all { it in selectedSsids }
                                                selectedSsids = if (allSelected) {
                                                    selectedSsids - all.toSet()
                                                } else {
                                                    selectedSsids + all
                                                }
                                            },
                                            minWidth = 48.dp,
                                            minHeight = 48.dp,
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.SelectAll,
                                                contentDescription = stringResource(R.string.action_select_all),
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (selectedSsids.isNotEmpty()) showDeleteSelectedAsk = true
                                            },
                                            minWidth = 48.dp,
                                            minHeight = 48.dp,
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Delete,
                                                contentDescription = stringResource(R.string.action_delete_selected),
                                                tint = MiuixTheme.colorScheme.error,
                                            )
                                        }
                                    },
                                )
                            } else {
                                TopAppBar(
                                    title = stringResource(R.string.app_name),
                                    largeTitle = stringResource(R.string.app_name),
                                    scrollBehavior = mappingScrollBehavior,
                                    actions = {
                                        IconButton(
                                            onClick = { openEditor(null) },
                                            minWidth = 48.dp,
                                            minHeight = 48.dp,
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Add,
                                                contentDescription = stringResource(R.string.action_add_mapping),
                                            )
                                        }
                                    },
                                )
                            }
                        } else {
                            TopAppBar(
                                title = stringResource(R.string.title_settings),
                                largeTitle = stringResource(R.string.title_settings),
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
                            label = stringResource(R.string.tab_mapping),
                        )
                        NavigationBarItem(
                            selected = tab == Tabs.SETTINGS,
                            onClick = {
                                Log.d(LOG_TAG, "tab -> settings")
                                exitSelection()
                                tab = Tabs.SETTINGS
                            },
                            icon = MiuixIcons.Settings,
                            label = stringResource(R.string.tab_settings),
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
                                    selectedSsids = if (m.ssid in selectedSsids) {
                                        selectedSsids - m.ssid
                                    } else {
                                        selectedSsids + m.ssid
                                    }
                                } else {
                                    openEditor(m)
                                }
                            },
                            onRowLongClick = { m ->
                                selecting = true
                                selectedSsids = selectedSsids + m.ssid
                            },
                            onAddClick = { openEditor(null) },
                        )
                    } else {
                        SettingsContent(
                            versionText = versionText,
                            themeMode = themeMode,
                            scrollBehavior = settingsScrollBehavior,
                            paddingValues = paddingValues,
                            onThemeChanged = onThemeChanged,
                            onOpenIcons = { page = RootPage.ICONS },
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
                            onShowGuide = {
                                Log.d(LOG_TAG, "show activation guide")
                                infoDismissed = false
                                tab = Tabs.MAPPING
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
                        title = stringResource(
                            if (editingOriginal == null) R.string.add_mapping_title else R.string.edit_mapping_title,
                        ),
                        onDismissRequest = { showEditor = false },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextField(
                                value = ssidInput,
                                onValueChange = { ssidInput = it },
                                label = stringResource(R.string.field_ssid),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            TextField(
                                value = labelInput,
                                onValueChange = { labelInput = it },
                                label = stringResource(R.string.field_label),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            val invalid = ssidInput.isBlank() || labelInput.isBlank()
                            if (invalid) {
                                Text(
                                    text = stringResource(R.string.error_required),
                                    fontSize = 12.sp,
                                    color = MiuixTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            }
                            TextButton(
                                text = stringResource(R.string.save),
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
                                text = stringResource(R.string.cancel),
                                onClick = { showEditor = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (deleteTarget != null) {
                    OverlayDialog(
                        show = deleteTarget != null,
                        title = stringResource(R.string.delete_mapping_title),
                        summary = deleteTarget,
                        onDismissRequest = { deleteTarget = null },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(
                                text = stringResource(R.string.delete),
                                onClick = {
                                    Log.d(LOG_TAG, "confirm delete ${deleteTarget}")
                                    deleteTarget?.let { onDelete(it) }
                                    deleteTarget = null
                                },
                                colors = dangerTextButtonColors(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                text = stringResource(R.string.cancel),
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
                        title = stringResource(R.string.delete_selected_title, count),
                        summary = stringResource(R.string.delete_selected_body),
                        onDismissRequest = { showDeleteSelectedAsk = false },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(
                                text = stringResource(R.string.delete),
                                onClick = {
                                    Log.d(LOG_TAG, "confirm delete-selected count=$count")
                                    onDeleteMany(selectedSsids.toList())
                                    showDeleteSelectedAsk = false
                                    exitSelection()
                                },
                                colors = dangerTextButtonColors(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                text = stringResource(R.string.cancel),
                                onClick = { showDeleteSelectedAsk = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (showImportAsk) {
                    OverlayDialog(
                        show = showImportAsk,
                        title = stringResource(R.string.import_dialog_title),
                        summary = stringResource(R.string.import_dialog_summary),
                        onDismissRequest = { showImportAsk = false },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(
                                text = stringResource(R.string.import_merge),
                                onClick = {
                                    Log.d(LOG_TAG, "import mode=merge")
                                    pendingReplace = false
                                    showImportAsk = false
                                    importPicker.launch(arrayOf("application/json"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                text = stringResource(R.string.import_replace),
                                onClick = {
                                    Log.d(LOG_TAG, "import mode=replace")
                                    pendingReplace = true
                                    showImportAsk = false
                                    importPicker.launch(arrayOf("application/json"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                text = stringResource(R.string.cancel),
                                onClick = { showImportAsk = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (showClearAllAsk) {
                    OverlayDialog(
                        show = showClearAllAsk,
                        title = stringResource(R.string.delete_all_title),
                        summary = stringResource(R.string.delete_all_body),
                        onDismissRequest = { showClearAllAsk = false },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(
                                text = stringResource(R.string.delete_all),
                                onClick = {
                                    Log.d(LOG_TAG, "confirm clear-all")
                                    onClearAll()
                                    showClearAllAsk = false
                                    exitSelection()
                                },
                                colors = dangerTextButtonColors(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(
                                text = stringResource(R.string.cancel),
                                onClick = { showClearAllAsk = false },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

            }
        }
    }
}
