package com.wifirename.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsContent(
    versionText: String,
    themeMode: String,
    scrollBehavior: ScrollBehavior,
    paddingValues: PaddingValues,
    onThemeChanged: (String) -> Unit,
    onOpenIcons: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearAll: () -> Unit,
    onShowGuide: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val themeOptions = listOf(
        stringResource(R.string.theme_system),
        stringResource(R.string.theme_light),
        stringResource(R.string.theme_dark),
    )
    val themeIndex = when (themeMode) {
        AppSettings.THEME_LIGHT -> 1
        AppSettings.THEME_DARK -> 2
        else -> 0
    }

    LazyColumn(
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
        item(key = "g1-title") {
            SmallTitle(text = stringResource(R.string.section_appearance))
        }
        item(key = "g1-card") {
            Card(insideMargin = PaddingValues(16.dp, 6.dp)) {
                OverlayDropdownPreference(
                    title = stringResource(R.string.setting_theme),
                    items = themeOptions,
                    selectedIndex = themeIndex,
                    onSelectedIndexChange = { index ->
                        onThemeChanged(
                            when (index) {
                                1 -> AppSettings.THEME_LIGHT
                                2 -> AppSettings.THEME_DARK
                                else -> AppSettings.THEME_SYSTEM
                            },
                        )
                    },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    thickness = 0.75.dp,
                    color = MiuixTheme.colorScheme.dividerLine,
                )
                ArrowPreference(
                    title = stringResource(R.string.settings_icons_colors),
                    summary = stringResource(R.string.settings_icons_colors_summary),
                    onClick = onOpenIcons,
                )
            }
        }

        item(key = "g2-title") {
            SmallTitle(text = stringResource(R.string.section_backup))
        }
        item(key = "g2-card") {
            Card(insideMargin = PaddingValues(16.dp, 6.dp)) {
                ArrowPreference(
                    title = stringResource(R.string.export_title),
                    summary = stringResource(R.string.export_summary),
                    onClick = onExport,
                )
                ArrowPreference(
                    title = stringResource(R.string.import_title),
                    summary = stringResource(R.string.import_summary),
                    onClick = onImport,
                )
            }
        }

        item(key = "g3-title") {
            SmallTitle(text = stringResource(R.string.section_other))
        }
        item(key = "g3-card") {
            Card(insideMargin = PaddingValues(16.dp, 6.dp)) {
                ArrowPreference(
                    title = stringResource(R.string.settings_guide),
                    onClick = onShowGuide,
                )
                ArrowPreference(
                    title = stringResource(R.string.clear_all_title),
                    summary = stringResource(R.string.clear_all_summary),
                    titleColor = BasicComponentDefaults.titleColor(
                        color = MiuixTheme.colorScheme.error,
                    ),
                    onClick = onClearAll,
                )
                ArrowPreference(
                    title = stringResource(R.string.settings_about),
                    onClick = onOpenAbout,
                    endActions = { Text(text = versionText) },
                )
            }
        }
    }
}
