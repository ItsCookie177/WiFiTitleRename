package com.wifirename.module

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

fun filterMappings(mappings: List<Mapping>, query: String): List<Mapping> {
    val q = query.trim()
    if (q.isEmpty()) return mappings
    return mappings.filter {
        it.label.contains(q, ignoreCase = true) || it.ssid.contains(q, ignoreCase = true)
    }
}

@Composable
fun MappingSearchPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .squircleClip(22.dp)
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = MiuixIcons.Basic.Search,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.search_hint),
            fontSize = 15.sp,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MappingSearchOverlay(
    show: Boolean,
    mappings: List<Mapping>,
    onDismiss: () -> Unit,
    onResult: (Mapping) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var query by rememberSaveable { mutableStateOf("") }
    var history by remember { mutableStateOf(emptyList<String>()) }
    var showClearAsk by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            history = SearchHistory.load(context)
            query = ""
            delay(80)
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
            showClearAsk = false
        }
    }

    fun rememberQuery() {
        if (query.isNotBlank()) {
            SearchHistory.save(context, query)
            history = SearchHistory.load(context)
        }
    }

    fun dismiss() {
        rememberQuery()
        focusManager.clearFocus()
        onDismiss()
    }

    BackHandler(enabled = show) {
        if (showClearAsk) showClearAsk = false else dismiss()
    }

    val results = remember(mappings, query) { filterMappings(mappings, query) }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(140)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background),
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .focusRequester(focusRequester),
                    insideMargin = DpSize(14.dp, 12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = MiuixTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        borderColor = MiuixTheme.colorScheme.surfaceContainerHigh,
                    ),
                    cornerRadius = 24.dp,
                    label = stringResource(R.string.search_hint),
                    useLabelAsPlaceholder = true,
                    leadingIcon = {
                        Icon(
                            imageVector = MiuixIcons.Basic.Search,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 14.dp, end = 8.dp)
                                .size(19.dp),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = { query = "" },
                                minWidth = 44.dp,
                                minHeight = 44.dp,
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Basic.Close,
                                    contentDescription = stringResource(R.string.action_clear_search),
                                    modifier = Modifier.size(17.dp),
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            rememberQuery()
                            focusManager.clearFocus()
                        },
                    ),
                    singleLine = true,
                )
                TextButton(
                    text = stringResource(R.string.cancel),
                    onClick = { dismiss() },
                    minWidth = 56.dp,
                    minHeight = 48.dp,
                    colors = ButtonDefaults.textButtonColors(
                        color = Color.Transparent,
                        textColor = MiuixTheme.colorScheme.primary,
                    ),
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .imePadding()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(AppDimens.CardSpacing),
                contentPadding = PaddingValues(
                    start = AppDimens.ScreenPadding,
                    end = AppDimens.ScreenPadding,
                    bottom = 24.dp,
                ),
            ) {
                if (query.isBlank()) {
                    item(key = "history-title") {
                        Text(
                            text = stringResource(R.string.search_history_title),
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (history.isEmpty()) {
                        item(key = "history-empty") {
                            Text(
                                text = stringResource(R.string.search_history_empty),
                                fontSize = 13.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    } else {
                        item(key = "history") {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                history.forEach { item ->
                                    Button(
                                        onClick = {
                                            query = item
                                            focusManager.clearFocus()
                                        },
                                        minWidth = 0.dp,
                                        minHeight = 36.dp,
                                        cornerRadius = 18.dp,
                                        colors = ButtonDefaults.buttonColors(
                                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                            contentColor = MiuixTheme.colorScheme.onSurface,
                                        ),
                                        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    ) {
                                        Text(text = item, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        item(key = "history-clear") {
                            TextButton(
                                text = stringResource(R.string.search_history_clear),
                                onClick = { showClearAsk = true },
                                colors = ButtonDefaults.textButtonColors(
                                    color = Color.Transparent,
                                    textColor = MiuixTheme.colorScheme.error,
                                ),
                            )
                        }
                    }
                } else if (results.isEmpty()) {
                    item(key = "empty") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Basic.Search,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                            Text(
                                text = stringResource(R.string.no_results_query, query.trim()),
                                modifier = Modifier.padding(top = 12.dp),
                                fontSize = 14.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    item(key = "results") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleClip(AppDimens.CardCorner),
                            insideMargin = PaddingValues(0.dp),
                        ) {
                            Column {
                                results.forEachIndexed { index, mapping ->
                                    MappingRow(
                                        mapping = mapping,
                                        selecting = false,
                                        checked = false,
                                        onClick = {
                                            rememberQuery()
                                            focusManager.clearFocus()
                                            onDismiss()
                                            onResult(mapping)
                                        },
                                        onLongClick = {},
                                    )
                                    if (index != results.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = AppDimens.CardInside),
                                            thickness = 0.75.dp,
                                            color = MiuixTheme.colorScheme.dividerLine,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showClearAsk) {
            val scrimInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = scrimInteraction,
                        indication = null,
                    ) {}
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.search_history_clear_title),
                            fontSize = 18.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.search_history_clear_body),
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        TextButton(
                            text = stringResource(R.string.search_history_clear),
                            onClick = {
                                SearchHistory.clear(context)
                                history = emptyList()
                                showClearAsk = false
                            },
                            colors = dangerTextButtonColors(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextButton(
                            text = stringResource(R.string.cancel),
                            onClick = { showClearAsk = false },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        }
    }
}
