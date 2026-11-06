package com.wifirename.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import top.yukonga.miuix.kmp.squircle.squircleClip
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
        verticalArrangement = Arrangement.spacedBy(AppDimens.CardSpacing),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 12.dp,
            bottom = paddingValues.calculateBottomPadding() + AppDimens.ContentBottom,
        ),
    ) {
        item(key = "g1-title") {
            SectionTitle(stringResource(R.string.section_appearance))
        }
        item(key = "g1-card") {
            SettingsCard {
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
                    modifier = Modifier.padding(horizontal = AppDimens.CardInside),
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
            SectionTitle(stringResource(R.string.section_backup))
        }
        item(key = "g2-card") {
            SettingsCard {
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
            SectionTitle(stringResource(R.string.section_other))
        }
        item(key = "g3-card") {
            SettingsCard {
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

@Composable
private fun SectionTitle(text: String) {
    SmallTitle(
        text = text,
        modifier = Modifier.padding(horizontal = AppDimens.ScreenPadding),
        insideMargin = PaddingValues(
            horizontal = AppDimens.CardInside,
            vertical = AppDimens.SectionTitleVertical,
        ),
    )
}

@Composable
private fun SettingsCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.ScreenPadding)
            .squircleClip(AppDimens.CardCorner),
        insideMargin = PaddingValues(0.dp),
        content = content,
    )
}
