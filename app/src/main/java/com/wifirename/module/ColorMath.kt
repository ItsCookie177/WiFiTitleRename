package com.wifirename.module

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal data class Hsl(val h: Float, val s: Float, val l: Float)

internal fun Color.toHsl(): Hsl {
    val r = red
    val g = green
    val b = blue
    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    val lightness = (max + min) / 2f
    val delta = max - min
    if (delta == 0f) return Hsl(0f, 0f, lightness)
    val saturation = if (lightness > 0.5f) {
        delta / (2f - max - min)
    } else {
        delta / (max + min)
    }
    val hue = when (max) {
        r -> ((g - b) / delta + if (g < b) 6f else 0f)
        g -> ((b - r) / delta + 2f)
        else -> ((r - g) / delta + 4f)
    } * 60f
    return Hsl(hue, saturation.coerceIn(0f, 1f), lightness.coerceIn(0f, 1f))
}

internal fun hslColor(
    hue: Float,
    saturation: Float,
    lightness: Float,
    alpha: Float = 1f,
): Color {
    val h = ((hue % 360f) + 360f) % 360f
    val s = saturation.coerceIn(0f, 1f)
    val l = lightness.coerceIn(0f, 1f)
    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val rgb = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(
        red = (rgb.first + m).coerceIn(0f, 1f),
        green = (rgb.second + m).coerceIn(0f, 1f),
        blue = (rgb.third + m).coerceIn(0f, 1f),
        alpha = alpha.coerceIn(0f, 1f),
    )
}

internal fun Color.toHexString(): String {
    val argb = toArgb()
    val alpha = (argb ushr 24) and 0xFF
    return if (alpha == 0xFF) {
        String.format(Locale.US, "#%06X", argb and 0xFFFFFF)
    } else {
        String.format(Locale.US, "#%08X", argb)
    }
}

internal fun parseHexColor(value: String): Color? {
    val text = value.trim().removePrefix("#")
    if (text.length != 6 && text.length != 8) return null
    if (text.any { it.digitToIntOrNull(16) == null }) return null
    val number = text.toLongOrNull(16) ?: return null
    return if (text.length == 6) {
        Color((0xFF000000L or number).toInt())
    } else {
        Color(number.toInt())
    }
}

internal fun contrastRatioOver(background: Color, foreground: Color): Float {
    val solidForeground = foreground.compositeOver(background)
    val first = solidForeground.luminance()
    val second = background.luminance()
    return (max(first, second) + 0.05f) / (min(first, second) + 0.05f)
}
