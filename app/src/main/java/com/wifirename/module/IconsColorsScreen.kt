package com.wifirename.module

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
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
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Check
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AppDimens.CardSpacing),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 12.dp,
                    bottom = paddingValues.calculateBottomPadding() + AppDimens.ContentBottom,
                ),
            ) {
                item(key = "active") {
                    ActiveIconCard(
                        activeIconId = activeIconId,
                        expanded = expanded,
                        onToggle = { expanded = !expanded },
                        modifier = Modifier.padding(horizontal = AppDimens.ScreenPadding),
                    )
                }
                if (expanded) {
                    item(key = "simple-title") {
                        SectionTitle(stringResource(R.string.icons_section_simple))
                    }
                    item(key = "simple-row") {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = AppDimens.ScreenPadding),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
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
                        SectionTitle(stringResource(R.string.icons_section_anime))
                    }
                    item(key = "anime-row") {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = AppDimens.ScreenPadding),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
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
                        SectionTitle(stringResource(R.string.icon_color_section))
                    }
                    item(key = "color-card") {
                        ColorTemplateCard(
                            activeIconId = activeIconId,
                            onIconSelected = onIconSelected,
                            onCustom = { showCustom = true },
                            modifier = Modifier.padding(horizontal = AppDimens.ScreenPadding),
                        )
                    }
                }
                item(key = "footer") {
                    Text(
                        text = stringResource(R.string.icons_footer),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(
                            start = AppDimens.ScreenPadding + AppDimens.CardInside,
                            end = AppDimens.ScreenPadding + AppDimens.CardInside,
                            top = 4.dp,
                        ),
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
private fun ActiveIconCard(
    activeIconId: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "active-chevron",
    )
    Card(
        onClick = onToggle,
        showIndication = true,
        modifier = modifier
            .fillMaxWidth()
            .squircleClip(AppDimens.CardCorner),
        insideMargin = PaddingValues(AppDimens.CardInside),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconPreview(
                iconId = activeIconId,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.width(AppDimens.CardInside))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(IconCatalog.labelRes(activeIconId)),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.icons_active_summary),
                    modifier = Modifier.padding(top = 2.dp),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.ExpandMore,
                    contentDescription = stringResource(
                        if (expanded) R.string.icons_collapse else R.string.icons_expand,
                    ),
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(rotation),
                    tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
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
            .clip(RoundedCornerShape(AppDimens.CardCorner))
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
            lineHeight = 15.sp,
            maxLines = 2,
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
    modifier: Modifier = Modifier,
) {
    val activeTemplate = IconCatalog.template(activeIconId) ?: IconCatalog.templates.first()
    val selectedIndex = IconCatalog.templates.indexOfFirst { it.id == activeIconId }
        .takeIf { it >= 0 } ?: 0
    val items = IconCatalog.templates.map { template ->
        DropdownItem(
            text = stringResource(template.nameRes),
            summary = stringResource(template.summaryRes),
            icon = { swatchModifier ->
                TemplateSwatch(template.swatch, swatchModifier)
            },
        )
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .squircleClip(AppDimens.CardCorner),
        insideMargin = PaddingValues(0.dp),
    ) {
        OverlaySpinnerPreference(
            items = items,
            selectedIndex = selectedIndex,
            title = stringResource(R.string.icon_color_template),
            summary = stringResource(activeTemplate.summaryRes),
            showValue = true,
            onSelectedIndexChange = { index ->
                IconCatalog.templates.getOrNull(index)?.let { onIconSelected(it.id) }
            },
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = AppDimens.CardInside),
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
