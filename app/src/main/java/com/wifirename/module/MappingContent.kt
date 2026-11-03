package com.wifirename.module

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val LOG_TAG = "WifiRenameUI"

@Composable
fun MappingContent(
    mappings: List<Mapping>,
    listState: LazyListState,
    scrollBehavior: ScrollBehavior,
    paddingValues: PaddingValues,
    infoDismissed: Boolean,
    onInfoDismiss: () -> Unit,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onCardClick: (Mapping) -> Unit,
    onDeleteClick: (Mapping) -> Unit,
) {
    val query = searchQuery.trim()
    val filtered = if (query.isEmpty()) mappings else mappings.filter {
        it.ssid.contains(query, ignoreCase = true) || it.label.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding(),
            bottom = paddingValues.calculateBottomPadding() + 16.dp,
        ),
    ) {
        if (!infoDismissed) {
            item(key = "info") {
                Card(
                    insideMargin = PaddingValues(14.dp, 12.dp),
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Aktifkan modul di Vector, centang scope Settings dan System UI, lalu restart System UI.",
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                        IconButton(onClick = onInfoDismiss) {
                            Icon(
                                imageVector = MiuixIcons.Basic.Close,
                                contentDescription = "Tutup",
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }

        if (mappings.size > 3) {
            item(key = "search") {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChanged,
                    label = "Cari SSID",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = MiuixIcons.Basic.Search,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }

        if (mappings.isEmpty()) {
            item(key = "empty") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = MiuixIcons.Basic.Search,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        modifier = Modifier.size(36.dp),
                    )
                    Text(
                        text = "Belum ada mapping. Tekan + untuk menambahkan.",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                }
            }
        } else if (filtered.isEmpty()) {
            item(key = "nomatch") {
                Text(
                    text = "Tidak ada yang cocok.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            }
        } else {
            items(filtered, key = { it.ssid }) { m ->
                MappingListCard(
                    mapping = m,
                    onClick = {
                        Log.d(LOG_TAG, "card clicked ${m.ssid}")
                        onCardClick(m)
                    },
                    onDeleteTap = {
                        Log.d(LOG_TAG, "delete clicked ${m.ssid}")
                        onDeleteClick(m)
                    },
                )
            }
        }
    }
}

@Composable
private fun MappingListCard(
    mapping: Mapping,
    onClick: () -> Unit,
    onDeleteTap: () -> Unit,
) {
    Card(onClick = onClick, insideMargin = PaddingValues(14.dp, 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = mapping.ssid,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
            IconButton(onClick = onDeleteTap) {
                Icon(
                    imageVector = MiuixIcons.Basic.Close,
                    contentDescription = "Hapus",
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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

@Composable
fun MappingFab(visible: Boolean, onClick: () -> Unit) {
    if (visible) {
        FloatingActionButton(onClick = onClick) {
            Text(
                text = "+",
                color = MiuixTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = text, fontSize = 16.sp)
    }
}
