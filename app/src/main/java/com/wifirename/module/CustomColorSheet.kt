package com.wifirename.module

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
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
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.Locale

@Composable
fun CustomColorSheet(
    show: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var bg by remember { mutableStateOf(IconSettings.defaultCustomBg) }
    var fg by remember { mutableStateOf(IconSettings.defaultCustomFg) }
    var target by rememberSaveable { mutableIntStateOf(0) }
    var mode by remember { mutableIntStateOf(1) }
    var hsl by remember { mutableStateOf(IconSettings.defaultCustomBg.toHsl()) }
    var alpha by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf("") }
    var showHelp by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val maxSheetHeight = with(LocalConfiguration.current) {
        (screenHeightDp * 0.9f).dp
    }

    fun currentColor(): Color = if (target == 0) bg else fg

    fun updateFromColor(color: Color) {
        if (target == 0) {
            bg = color.copy(alpha = 1f)
        } else {
            fg = color
        }
        hsl = color.toHsl()
        alpha = color.alpha
        hexInput = color.toHexString()
    }

    fun applyHsl() {
        val color = hslColor(
            hue = hsl.h,
            saturation = hsl.s,
            lightness = hsl.l,
            alpha = if (target == 0) 1f else alpha,
        )
        if (target == 0) bg = color else fg = color
        hexInput = color.toHexString()
    }

    fun switchTarget(newTarget: Int) {
        target = newTarget
        val color = if (newTarget == 0) bg else fg
        hsl = color.toHsl()
        alpha = color.alpha
        hexInput = color.toHexString()
    }

    fun resetColors() {
        bg = IconSettings.defaultCustomBg
        fg = IconSettings.defaultCustomFg
        val color = currentColor()
        hsl = color.toHsl()
        alpha = color.alpha
        hexInput = color.toHexString()
    }

    LaunchedEffect(show) {
        if (show) {
            target = 0
            bg = IconSettings.customBgColor(context).copy(alpha = 1f)
            fg = IconSettings.customFgColor(context)
            hsl = bg.toHsl()
            alpha = 1f
            hexInput = bg.toHexString()
            mode = AppSettings.iconColorMode(context)
        }
    }

    val current = currentColor()
    val contrast = contrastRatioOver(bg, fg)
    val hueColors = remember {
        listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map { hue ->
            hslColor(hue, 1f, 0.5f)
        }
    }

    OverlayBottomSheet(
        show = show,
        title = stringResource(R.string.icon_color_custom_title),
        endAction = {
            IconButton(
                onClick = { resetColors() },
                minWidth = 40.dp,
                minHeight = 40.dp,
            ) {
                Icon(
                    imageVector = MiuixIcons.Reset,
                    contentDescription = stringResource(R.string.icon_color_reset),
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        insideMargin = DpSize(AppDimens.ScreenPadding, 0.dp),
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxSheetHeight)
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CustomPreview(
                            bg = bg,
                            fg = fg,
                            circle = false,
                            modifier = Modifier.size(64.dp),
                        )
                        Text(
                            text = stringResource(R.string.icon_color_preview_squircle),
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CustomPreview(
                            bg = bg,
                            fg = fg,
                            circle = true,
                            modifier = Modifier.size(64.dp),
                        )
                        Text(
                            text = stringResource(R.string.icon_color_preview_circle),
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.icon_color_shortcut_explained),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                )
            }

            SegmentedControl(
                options = listOf(
                    stringResource(R.string.icon_color_background),
                    stringResource(R.string.icon_color_glyph),
                ),
                selectedIndex = target,
                onSelected = { switchTarget(it) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorSwatch(
                    color = current,
                    modifier = Modifier.size(52.dp),
                )
                TextField(
                    value = hexInput,
                    onValueChange = { raw ->
                        val text = raw.trim().uppercase(Locale.US).take(9)
                        hexInput = text
                        parseHexColor(text)?.let { parsed ->
                            if (target == 0) {
                                bg = parsed.copy(alpha = 1f)
                                hsl = bg.toHsl()
                                alpha = 1f
                            } else {
                                fg = parsed
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
            }
            if (parseHexColor(hexInput) == null) {
                Text(
                    text = stringResource(R.string.icon_color_hex_invalid),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.error,
                )
            }
            if (target == 0) {
                Text(
                    text = stringResource(R.string.icon_color_bg_opaque_note),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
                if (target == 1) {
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
                    color = current,
                    onColorChange = { updateFromColor(it) },
                )
                if (target == 1) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.ButtonSpacing),
            ) {
                TextButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(R.string.icon_color_apply),
                    onClick = {
                        val solidBg = bg.copy(alpha = 1f)
                        IconSettings.setCustomColors(context, solidBg, fg)
                        when (IconSwitcher.pinCustomShortcut(context, solidBg, fg)) {
                            PinResult.REQUESTED -> {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.icon_color_shortcut_sent),
                                    Toast.LENGTH_LONG,
                                ).show()
                                onDismiss()
                            }

                            PinResult.UNSUPPORTED, PinResult.FAILED -> showHelp = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
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
        modifier.squircleClip(22.dp)
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
