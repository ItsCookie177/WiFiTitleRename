package com.wifirename.module

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Reset
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.Locale

enum class ColorTarget(
    val titleRes: Int,
    val isBackground: Boolean,
) {
    BACKGROUND(R.string.color_target_background, true),
    GLYPH(R.string.color_target_glyph, false),
}

@Composable
fun ColorEditDialog(
    target: ColorTarget?,
    savedBg: Color,
    savedFg: Color,
    onDismiss: () -> Unit,
    onSave: (Color, Color) -> Unit,
) {
    if (target == null) return

    val context = LocalContext.current
    val current = if (target.isBackground) savedBg else savedFg
    var bgDraft by remember(target) { mutableStateOf(savedBg.copy(alpha = 1f)) }
    var fgDraft by remember(target) { mutableStateOf(savedFg) }
    var hsl by remember(target) { mutableStateOf(current.toHsl()) }
    var alpha by remember(target) { mutableFloatStateOf(if (target.isBackground) 1f else current.alpha) }
    var hexInput by remember(target) { mutableStateOf(current.toHexString()) }
    var mode by rememberSaveable(target) { mutableIntStateOf(AppSettings.iconColorMode(context)) }
    val scrollState = rememberScrollState()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.68f).dp

    fun draftColor(): Color = if (target.isBackground) bgDraft else fgDraft

    fun applyDraft(color: Color) {
        val normalized = if (target.isBackground) color.copy(alpha = 1f) else color
        if (target.isBackground) bgDraft = normalized else fgDraft = normalized
        hsl = normalized.toHsl()
        alpha = normalized.alpha
        hexInput = normalized.toHexString()
    }

    fun applyHsl() {
        val color = hslColor(
            hue = hsl.h,
            saturation = hsl.s,
            lightness = hsl.l,
            alpha = if (target.isBackground) 1f else alpha,
        )
        if (target.isBackground) bgDraft = color else fgDraft = color
        hexInput = color.toHexString()
    }

    fun resetDraft() {
        applyDraft(
            if (target.isBackground) IconSettings.defaultCustomBg else IconSettings.defaultCustomFg,
        )
    }

    val previewBg = bgDraft.copy(alpha = 1f)
    val previewFg = fgDraft
    val contrast = contrastRatioOver(previewBg, previewFg)
    val hueColors = remember {
        listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map { hue ->
            hslColor(hue, 1f, 0.5f)
        }
    }
    val validHex = parseHexColor(hexInput) != null

    OverlayDialog(
        show = true,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(target.titleRes),
                    modifier = Modifier.weight(1f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                IconButton(
                    onClick = { resetDraft() },
                    minWidth = 40.dp,
                    minHeight = 40.dp,
                ) {
                    Icon(
                        imageVector = MiuixIcons.Reset,
                        contentDescription = stringResource(R.string.icon_color_reset),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = hexInput,
                    onValueChange = { raw ->
                        val text = raw.trim().uppercase(Locale.US).take(9)
                        hexInput = text
                        parseHexColor(text)?.let { parsed ->
                            if (target.isBackground) {
                                val opaque = parsed.copy(alpha = 1f)
                                bgDraft = opaque
                                hsl = opaque.toHsl()
                                alpha = 1f
                                hexInput = opaque.toHexString()
                            } else {
                                fgDraft = parsed
                                hsl = parsed.toHsl()
                                alpha = parsed.alpha
                            }
                        }
                    },
                    label = stringResource(R.string.icon_color_hex),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.Characters,
                    ),
                )
                CustomAdaptivePreview(
                    bg = previewBg,
                    fg = previewFg,
                    circle = false,
                    modifier = Modifier.size(54.dp),
                )
                CustomAdaptivePreview(
                    bg = previewBg,
                    fg = previewFg,
                    circle = true,
                    modifier = Modifier.size(54.dp),
                )
            }

            if (!validHex) {
                Text(
                    text = stringResource(R.string.icon_color_hex_invalid),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.error,
                )
            }

            SegmentedControl(
                options = listOf(
                    stringResource(R.string.icon_color_mode_grid),
                    stringResource(R.string.icon_color_mode_slider),
                ),
                selectedIndex = mode,
                onSelected = {
                    mode = it
                    AppSettings.setIconColorMode(context, it)
                },
            )

            if (mode == 1) {
                ColorValueSlider(
                    label = stringResource(R.string.icon_color_hue),
                    valueText = DegreesValue(hsl.h),
                    value = hsl.h / 360f,
                    onValueChange = { fraction ->
                        hsl = hsl.copy(h = (fraction * 360f).coerceIn(0f, 360f))
                        applyHsl()
                    },
                    colors = hueColors,
                )
                ColorValueSlider(
                    label = stringResource(R.string.icon_color_saturation),
                    valueText = PercentValue(hsl.s),
                    value = hsl.s,
                    onValueChange = { fraction ->
                        hsl = hsl.copy(s = fraction)
                        applyHsl()
                    },
                    colors = listOf(
                        hslColor(hsl.h, 0f, hsl.l),
                        hslColor(hsl.h, 1f, hsl.l),
                    ),
                )
                ColorValueSlider(
                    label = stringResource(R.string.icon_color_lightness),
                    valueText = PercentValue(hsl.l),
                    value = hsl.l,
                    onValueChange = { fraction ->
                        hsl = hsl.copy(l = fraction)
                        applyHsl()
                    },
                    colors = listOf(
                        Color.Black,
                        hslColor(hsl.h, hsl.s, 0.5f),
                        Color.White,
                    ),
                )
                if (!target.isBackground) {
                    ColorValueSlider(
                        label = stringResource(R.string.icon_color_opacity),
                        valueText = PercentValue(alpha),
                        value = alpha,
                        onValueChange = { fraction ->
                            alpha = fraction
                            applyHsl()
                        },
                        colors = listOf(
                            hslColor(hsl.h, hsl.s, hsl.l, 0f),
                            hslColor(hsl.h, hsl.s, hsl.l, 1f),
                        ),
                        checkerboard = true,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.icon_color_palette),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                PaletteGrid(
                    color = draftColor(),
                    onColorChange = { color -> applyDraft(color) },
                )
                if (!target.isBackground) {
                    ColorValueSlider(
                        label = stringResource(R.string.icon_color_opacity),
                        valueText = PercentValue(alpha),
                        value = alpha,
                        onValueChange = { fraction ->
                            alpha = fraction
                            applyHsl()
                        },
                        colors = listOf(
                            hslColor(hsl.h, hsl.s, hsl.l, 0f),
                            hslColor(hsl.h, hsl.s, hsl.l, 1f),
                        ),
                        checkerboard = true,
                    )
                }
            }

            if (contrast < 3f) {
                Text(
                    text = stringResource(R.string.icon_color_contrast_warning),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.error,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.ButtonSpacing),
            ) {
                TextButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(R.string.save),
                    onClick = {
                        val finalBg = bgDraft.copy(alpha = 1f)
                        val finalFg = fgDraft
                        onSave(finalBg, finalFg)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = validHex,
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
            Spacer(Modifier.size(2.dp))
        }
    }
}

@Composable
private fun CustomAdaptivePreview(
    bg: Color,
    fg: Color,
    circle: Boolean,
    modifier: Modifier,
) {
    val shaped = if (circle) {
        modifier.clip(CircleShape)
    } else {
        modifier.squircleClip(16.dp)
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
