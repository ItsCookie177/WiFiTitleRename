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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.squircle.squircleClip
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
    selectedSsids: Set<String>,
    onRowClick: (Mapping) -> Unit,
    onRowLongClick: (Mapping) -> Unit,
    onAddClick: () -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(AppDimens.CardSpacing),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 12.dp,
            bottom = paddingValues.calculateBottomPadding() + AppDimens.ContentBottom,
        ),
    ) {
        if (!selecting) {
            item(key = "search") {
                val focusManager = LocalFocusManager.current
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.ScreenPadding)
                        .heightIn(min = 52.dp),
                    insideMargin = DpSize(16.dp, 15.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = MiuixTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    ),
                    cornerRadius = 26.dp,
                    label = stringResource(R.string.search_hint),
                    useLabelAsPlaceholder = true,
                    leadingIcon = {
                        Icon(
                            imageVector = MiuixIcons.Basic.Search,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 10.dp)
                                .size(20.dp),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchChanged("") },
                                minWidth = 48.dp,
                                minHeight = 48.dp,
                                modifier = Modifier.padding(end = 4.dp),
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Basic.Close,
                                    contentDescription = stringResource(R.string.action_clear_search),
                                    modifier = Modifier.size(18.dp),
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() },
                    ),
                    singleLine = true,
                )
            }
        }

        if (!infoDismissed) {
            item(key = "info") {
                InfoCard(
                    onDismiss = onInfoDismiss,
                    modifier = Modifier.padding(horizontal = AppDimens.ScreenPadding),
                )
            }
        }

        if (mappings.isEmpty() && !selecting) {
            item(key = "empty") {
                EmptyCard(
                    modifier = Modifier.padding(horizontal = AppDimens.ScreenPadding),
                )
            }
        }

        item(key = "group") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.ScreenPadding)
                    .squircleClip(AppDimens.CardCorner),
                insideMargin = PaddingValues(0.dp),
            ) {
                Column {
                    if (mappings.isEmpty() && !selecting) {
                        AddMappingRow(onClick = onAddClick)
                    } else if (visibleMappings.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_results_query, searchQuery.trim()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            textAlign = TextAlign.Center,
                        )
                        if (!selecting) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = AppDimens.CardInside),
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
                                    modifier = Modifier.padding(horizontal = AppDimens.CardInside),
                                    thickness = 0.75.dp,
                                    color = MiuixTheme.colorScheme.dividerLine,
                                )
                            }
                        }
                        if (!selecting) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = AppDimens.CardInside),
                                thickness = 0.75.dp,
                                color = MiuixTheme.colorScheme.dividerLine,
                            )
                            AddMappingRow(onClick = onAddClick)
                        }
                    }
                }
            }
        }

        if (!selecting && mappings.isNotEmpty() && searchQuery.isBlank()) {
            item(key = "hint") {
                Text(
                    text = stringResource(R.string.summary_hint),
                    modifier = Modifier.padding(horizontal = AppDimens.ScreenPadding),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        insideMargin = PaddingValues(AppDimens.CardInside),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
            )
            Text(
                text = stringResource(R.string.subtitle_original, mapping.ssid),
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
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
private fun EmptyCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        insideMargin = PaddingValues(AppDimens.CardInside),
    ) {
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
                modifier = Modifier.padding(top = 4.dp, start = AppDimens.CardInside, end = AppDimens.CardInside),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
