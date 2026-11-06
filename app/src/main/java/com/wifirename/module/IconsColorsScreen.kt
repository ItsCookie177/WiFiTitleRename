package com.wifirename.module

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.ColorPicker
import top.yukonga.miuix.kmp.basic.ColorSpace
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Check
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.Locale

@Composable
fun IconsColorsScreen(
    activeIconId: String,
    onIconSelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(enabled = true) { onBack() }
    val scrollBehavior = MiuixScrollBehavior()
    var expanded by rememberSaveable { mutableStateOf(true) }
    var showCustom by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.icons_page_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        minWidth = 48.dp,
                        minHeight = 48.dp,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 12.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                ),
            ) {
                item(key = "active") {
                    ActiveIconCard(
                        activeIconId = activeIconId,
                        expanded = expanded,
                        onToggle = { expanded = !expanded },
                    )
                }
                if (expanded) {
                    item(key = "simple-title") {
                        SmallTitle(text = stringResource(R.string.icons_section_simple))
                    }
                    item(key = "simple-row") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(IconCatalog.templates, key = { it.id }) { template ->
                                IconTile(
                                    iconId = template.id,
                                    label = stringResource(template.nameRes),
                                    selected = template.id == activeIconId,
                                    onClick = { onIconSelected(template.id) },
                                )
                            }
                        }
                    }
                    item(key = "anime-title") {
                        SmallTitle(text = stringResource(R.string.icons_section_anime))
                    }
                    item(key = "anime-row") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(IconCatalog.extras, key = { it.id }) { extra ->
                                IconTile(
                                    iconId = extra.id,
                                    label = stringResource(extra.nameRes),
                                    selected = extra.id == activeIconId,
                                    onClick = { onIconSelected(extra.id) },
                                )
                            }
                        }
                    }
                    item(key = "color-title") {
                        SmallTitle(text = stringResource(R.string.icon_color_section))
                    }
                    item(key = "color-card") {
                        ColorTemplateCard(
                            activeIconId = activeIconId,
                            onIconSelected = onIconSelected,
                            onCustom = { showCustom = true },
                        )
                    }
                }
                item(key = "footer") {
                    Text(
                        text = stringResource(R.string.icons_footer),
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp),
                    )
                }
            }
            CustomColorSheet(
                show = showCustom,
                onDismiss = { showCustom = false },
            )
        }
    }
}

@Composable
private fun ActiveIconCard(
    activeIconId: String,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(insideMargin = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconPreview(
                iconId = activeIconId,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(IconCatalog.labelRes(activeIconId)),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.icons_active_summary),
                    modifier = Modifier.padding(top = 2.dp),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            IconButton(
                onClick = onToggle,
                minWidth = 48.dp,
                minHeight = 48.dp,
            ) {
                Icon(
                    imageVector = if (expanded) MiuixIcons.ExpandLess else MiuixIcons.ExpandMore,
                    contentDescription = stringResource(
                        if (expanded) R.string.icons_collapse else R.string.icons_expand,
                    ),
                )
            }
        }
    }
}

@Composable
private fun IconTile(
    iconId: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(6.dp)
            .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .then(
                    if (selected) {
                        Modifier.border(
                            width = 2.dp,
                            color = MiuixTheme.colorScheme.primary,
                            shape = RoundedCornerShape(22.dp),
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(3.dp),
        ) {
            IconPreview(
                iconId = iconId,
                modifier = Modifier.fillMaxSize(),
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MiuixTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MiuixIcons.Basic.Check,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MiuixTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun IconPreview(
    iconId: String,
    modifier: Modifier = Modifier,
) {
    val template = IconCatalog.template(iconId)
    if (template != null) {
        Box(modifier.squircleClip(20.dp)) {
            Image(
                painter = painterResource(template.bgRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
            Image(
                painter = painterResource(template.fgRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        }
        return
    }
    val extra = IconCatalog.extra(iconId) ?: return
    Box(modifier.squircleClip(20.dp)) {
        Image(
            painter = painterResource(extra.imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun ColorTemplateCard(
    activeIconId: String,
    onIconSelected: (String) -> Unit,
    onCustom: () -> Unit,
) {
    val active = IconCatalog.template(activeIconId)
    val selectedIndex = IconCatalog.templates.indexOfFirst { it.id == activeIconId }
    val items = IconCatalog.templates.map { template ->
        DropdownItem(
            text = stringResource(template.nameRes),
            summary = stringResource(template.summaryRes),
            icon = { modifier ->
                TemplateSwatch(template.swatch, modifier)
            },
        )
    }
    Card(insideMargin = PaddingValues(16.dp, 6.dp)) {
        OverlaySpinnerPreference(
            items = items,
            selectedIndex = selectedIndex,
            title = stringResource(R.string.icon_color_template),
            summary = active?.let { stringResource(it.summaryRes) },
            onSelectedIndexChange = { index ->
                IconCatalog.templates.getOrNull(index)?.let { onIconSelected(it.id) }
            },
        )
        HorizontalDivider(
            modifier = Modifier.padding(start = 16.dp),
            thickness = 0.75.dp,
            color = MiuixTheme.colorScheme.dividerLine,
        )
        ArrowPreference(
            title = stringResource(R.string.icon_color_custom),
            summary = stringResource(R.string.icon_color_custom_summary),
            onClick = onCustom,
        )
    }
}

@Composable
private fun TemplateSwatch(colors: List<Color>, modifier: Modifier) {
    val brush = if (colors.size > 1) {
        Brush.linearGradient(colors)
    } else {
        SolidColor(colors.firstOrNull() ?: Color.Transparent)
    }
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(brush),
    )
}

@Composable
private fun CustomColorSheet(
    show: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var bg by remember { mutableStateOf(IconSettings.customBgColor(context)) }
    var fg by remember { mutableStateOf(IconSettings.customFgColor(context)) }
    var target by rememberSaveable { mutableIntStateOf(0) }
    var bgHex by remember { mutableStateOf(bg.toHex()) }
    var fgHex by remember { mutableStateOf(fg.toHex()) }
    var showHelp by remember { mutableStateOf(false) }

    OverlayBottomSheet(
        show = show,
        title = stringResource(R.string.icon_color_custom_title),
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CustomPreview(bg = bg, fg = fg, circle = false, modifier = Modifier.size(96.dp))
                CustomPreview(bg = bg, fg = fg, circle = true, modifier = Modifier.size(96.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = stringResource(R.string.icon_color_preview_squircle),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = stringResource(R.string.icon_color_preview_circle),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            if (IconSwitcher.contrastRatio(bg, fg) < 3f) {
                Text(
                    text = stringResource(R.string.icon_color_contrast_warning),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.error,
                )
            }
            Text(
                text = stringResource(R.string.icon_color_shortcut_explained),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            TabRow(
                tabs = listOf(
                    stringResource(R.string.icon_color_background),
                    stringResource(R.string.icon_color_glyph),
                ),
                selectedTabIndex = target,
                onTabSelected = { target = it },
            )
            val current = if (target == 0) bg else fg
            ColorPicker(
                color = current,
                onColorChanged = { color ->
                    if (target == 0) {
                        bg = color
                        bgHex = color.toHex()
                        IconSettings.setCustomColors(context, color, fg)
                    } else {
                        fg = color
                        fgHex = color.toHex()
                        IconSettings.setCustomColors(context, bg, color)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                showPreview = false,
                colorSpace = ColorSpace.OKLCH,
            )
            val hex = if (target == 0) bgHex else fgHex
            TextField(
                value = hex,
                onValueChange = { value ->
                    val upper = value.uppercase(Locale.US)
                    if (target == 0) {
                        bgHex = upper
                    } else {
                        fgHex = upper
                    }
                    parseHex(upper)?.let { parsed ->
                        if (target == 0) {
                            bg = parsed
                            IconSettings.setCustomColors(context, parsed, fg)
                        } else {
                            fg = parsed
                            IconSettings.setCustomColors(context, bg, parsed)
                        }
                    }
                },
                label = stringResource(R.string.icon_color_hex),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (parseHex(hex) == null) {
                Text(
                    text = stringResource(R.string.icon_color_hex_invalid),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(R.string.icon_color_install_shortcut),
                    onClick = {
                        IconSettings.setCustomColors(context, bg, fg)
                        when (IconSwitcher.pinCustomShortcut(context, bg, fg)) {
                            PinResult.REQUESTED -> Toast.makeText(
                                context,
                                context.getString(R.string.icon_color_shortcut_sent),
                                Toast.LENGTH_LONG,
                            ).show()

                            PinResult.UNSUPPORTED, PinResult.FAILED -> showHelp = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    OverlayDialog(
        show = showHelp,
        title = stringResource(R.string.icon_color_shortcut_help_title),
        summary = stringResource(R.string.icon_color_shortcut_help_body),
        onDismissRequest = { showHelp = false },
    ) {
        TextButton(
            text = stringResource(R.string.ok),
            onClick = { showHelp = false },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CustomPreview(
    bg: Color,
    fg: Color,
    circle: Boolean,
    modifier: Modifier,
) {
    val shaped = if (circle) {
        modifier.clip(CircleShape)
    } else {
        modifier.squircleClip(26.dp)
    }
    Box(shaped.background(bg)) {
        Image(
            painter = painterResource(R.drawable.ic_caret_wave_glyph),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(fg),
        )
    }
}

private fun Color.toHex(): String =
    String.format(Locale.US, "#%06X", toArgb() and 0xFFFFFF)

private fun parseHex(value: String): Color? {
    val text = value.trim().removePrefix("#")
    if (text.length != 6) return null
    val rgb = text.toLongOrNull(16) ?: return null
    return Color((0xFF000000L or rgb).toInt())
}
