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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
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
            SummaryCard(count = mappings.size, selecting = selecting)
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
                    insideMargin = DpSize(16.dp, 12.dp),
                    label = stringResource(R.string.search_hint),
                    leadingIcon = {
                        Icon(
                            imageVector = MiuixIcons.Basic.Search,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 2.dp, end = 8.dp).size(18.dp),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                    singleLine = true,
                )
            }
        }

        if (mappings.isEmpty() && !selecting) {
            item(key = "empty") {
                EmptyCard()
            }
        }

        item(key = "group") {
            Card(insideMargin = PaddingValues(0.dp)) {
                Column {
                    if (mappings.isEmpty() && !selecting) {
                        AddMappingRow(onClick = onAddClick)
                    } else if (visibleMappings.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_results),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            textAlign = TextAlign.Center,
                        )
                        if (!selecting) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                thickness = 0.75.dp,
                                color = MiuixTheme.colorScheme.dividerLine,
                            )
                            AddMappingRow(onClick = onAddClick)
                        }
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
                        if (!selecting) {
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
}

@Composable
private fun SummaryCard(count: Int, selecting: Boolean) {
    Card(insideMargin = PaddingValues(16.dp, 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    text = stringResource(R.string.mapping_count, count),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        if (selecting) R.string.summary_hint_selecting else R.string.summary_hint,
                    ),
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
                    text = stringResource(R.string.guide_title),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.guide_body),
                    modifier = Modifier.padding(top = 2.dp),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            }
            IconButton(
                onClick = onDismiss,
                minWidth = 48.dp,
                minHeight = 48.dp,
            ) {
                Icon(
                    imageVector = MiuixIcons.Basic.Close,
                    contentDescription = stringResource(R.string.action_dismiss_guide),
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
            .heightIn(min = 56.dp)
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
                text = stringResource(R.string.subtitle_original, mapping.ssid),
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
            .heightIn(min = 56.dp)
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
            text = stringResource(R.string.add_mapping),
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
                text = stringResource(R.string.empty_title),
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.empty_body),
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
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 16.sp)
    }
}
