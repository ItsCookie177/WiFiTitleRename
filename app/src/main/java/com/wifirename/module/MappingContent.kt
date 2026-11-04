package com.wifirename.module

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.ui.state.ToggleableState

fun filterMappings(mappings: List<Mapping>, query: String): List<Mapping> {
    val q = query.trim()
    if (q.isEmpty()) return mappings
    return mappings.filter {
        it.label.contains(q, ignoreCase = true) || it.ssid.contains(q, ignoreCase = true)
    }
}

@Composable
fun MappingContent(
    mappings: List<Mapping>,
    visibleMappings: List<Mapping>,
    listState: LazyListState,
    scrollBehavior: ScrollBehavior,
    paddingValues: PaddingValues,
    infoDismissed: Boolean,
    onInfoDismiss: () -> Unit,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    selecting: Boolean,
    selectedSsids: List<String>,
    onRowClick: (Mapping) -> Unit,
    onRowLongClick: (Mapping) -> Unit,
    onAddClick: () -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding() + 12.dp,
            bottom = paddingValues.calculateBottomPadding() + 24.dp,
        ),
    ) {
        item(key = "summary") {
            SummaryCard(count = mappings.size)
        }

        if (!infoDismissed) {
            item(key = "info") {
                InfoCard(onDismiss = onInfoDismiss)
            }
        }

        if (mappings.size > 3 || searchQuery.isNotEmpty()) {
            item(key = "search") {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = "Cari nama WiFi",
                    leadingIcon = {
                        Icon(
                            imageVector = MiuixIcons.Basic.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                    singleLine = true,
                )
            }
        }

        if (mappings.isEmpty()) {
            item(key = "empty") {
                EmptyCard()
            }
        }

        item(key = "group") {
            Card(insideMargin = PaddingValues(0.dp)) {
                Column {
                    if (mappings.isEmpty()) {
                        AddMappingRow(onClick = onAddClick)
                    } else if (visibleMappings.isEmpty()) {
                        Text(
                            text = "Tidak ada mapping yang cocok",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            textAlign = TextAlign.Center,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            thickness = 0.75.dp,
                            color = MiuixTheme.colorScheme.dividerLine,
                        )
                        AddMappingRow(onClick = onAddClick)
                    } else {
                        visibleMappings.forEachIndexed { index, mapping ->
                            MappingRow(
                                mapping = mapping,
                                selecting = selecting,
                                checked = mapping.ssid in selectedSsids,
                                onClick = { onRowClick(mapping) },
                                onLongClick = { onRowLongClick(mapping) },
                            )
                            if (index != visibleMappings.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp),
                                    thickness = 0.75.dp,
                                    color = MiuixTheme.colorScheme.dividerLine,
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            thickness = 0.75.dp,
                            color = MiuixTheme.colorScheme.dividerLine,
                        )
                        AddMappingRow(onClick = onAddClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(count: Int) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.ListView,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$count mapping",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Ketuk untuk edit, tahan untuk memilih",
                    modifier = Modifier.padding(top = 2.dp),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoCard(onDismiss: () -> Unit) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Aktifkan modul di Vector/LSPosed",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Lalu restart SystemUI agar nama WiFi berubah.",
                    modifier = Modifier.padding(top = 2.dp),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = MiuixIcons.Basic.Close,
                    contentDescription = "Tutup info",
                    modifier = Modifier.size(16.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MappingRow(
    mapping: Mapping,
    selecting: Boolean,
    checked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selecting) {
            Checkbox(
                state = if (checked) ToggleableState.On else ToggleableState.Off,
                onClick = null,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mapping.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Asli: ${mapping.ssid}",
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddMappingRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MiuixIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MiuixTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Tambah mapping",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyCard() {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.ListView,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
            Text(
                text = "Belum ada mapping",
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Text(
                text = "Tambahkan SSID dan nama tampilan baru untuk mulai mengganti judul WiFi.",
                modifier = Modifier.padding(top = 4.dp, start = 24.dp, end = 24.dp),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 16.sp)
    }
}
