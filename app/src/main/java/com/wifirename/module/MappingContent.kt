package com.wifirename.module

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding() + 12.dp,
            bottom = paddingValues.calculateBottomPadding() + 20.dp,
        ),
    ) {
        if (!infoDismissed) {
            item(key = "info") {
                Card(insideMargin = PaddingValues(16.dp, 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Aktifkan modul di Vector, centang scope Settings dan System UI, lalu restart System UI.",
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                        IconButton(
                            onClick = {
                                Log.d(LOG_TAG, "info card dismissed")
                                onInfoDismiss()
                            },
                            minWidth = 40.dp,
                            minHeight = 40.dp,
                        ) {
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
                    label = "Cari nama WiFi",
                    modifier = Modifier.fillMaxWidth(),
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
                        .padding(top = 72.dp),
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
                        text = "Belum ada mapping.\nTekan + untuk menambahkan.",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
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
    Card(onClick = onClick, insideMargin = PaddingValues(16.dp, 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = mapping.ssid,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
            IconButton(
                onClick = onDeleteTap,
                minWidth = 40.dp,
                minHeight = 40.dp,
            ) {
                Icon(
                    imageVector = MiuixIcons.Basic.Close,
                    contentDescription = "Hapus",
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp, 18.dp),
            )
            Text(
                text = mapping.label,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
        }
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
