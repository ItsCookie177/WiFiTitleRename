/*
 * About page background adapted from HyperCeiler
 * https://github.com/ReChronoRain/HyperCeiler
 * Copyright (C) 2023-2026 HyperCeiler Contributions
 * Licensed under the GNU Affero General Public License v3.0 (AGPL-3.0).
 *
 * BgEffectPainter / BgEffectDataManager ported to Compose. The AGSL shader lives in
 * res/raw/bg_frag.glsl. The shader is evaluated through a RuntimeShader used as a
 * Paint shader (API 33+); older versions fall back to a static gradient from the
 * same palette. The palette cycling and the 120 s ping-pong match HyperCeiler.
 */
package com.wifirename.module

import android.content.Context
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import kotlin.math.floor
import kotlin.math.roundToInt

private const val PingPongSeconds = 120f
private const val TargetFrameSeconds = 1f / 30f

private class BgEffectData(
    val points: FloatArray,
    val alphaMulti: Float,
    val noiseScale: Float,
    val pointOffset: Float,
    val pointRadiusMulti: Float,
    val saturateOffset: Float,
    val lightOffset: Float,
    val alphaOffset: Float,
    val shadowColorMulti: Float,
    val shadowColorOffset: Float,
    val shadowNoiseScale: Float,
    val shadowOffset: Float,
    val translateY: Float,
    val colorInterpPeriod: Float,
    val gradientSpeedRest: Float,
    val colors1: FloatArray,
    val colors2: FloatArray,
    val colors3: FloatArray,
)

// BgEffectDataManager.mPhoneLightData
private val PhoneLightData = BgEffectData(
    points = floatArrayOf(0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f),
    alphaMulti = 1.0f,
    noiseScale = 1.5f,
    pointOffset = 0.2f,
    pointRadiusMulti = 1.0f,
    saturateOffset = 0.2f,
    lightOffset = 0.1f,
    alphaOffset = 0.5f,
    shadowColorMulti = 0.3f,
    shadowColorOffset = 0.3f,
    shadowNoiseScale = 5.0f,
    shadowOffset = 0.01f,
    translateY = 0.0f,
    colorInterpPeriod = 5.0f,
    gradientSpeedRest = 1.05f,
    colors1 = floatArrayOf(1.0f, 0.9f, 0.94f, 1.0f, 1.0f, 0.84f, 0.89f, 1.0f, 0.97f, 0.73f, 0.82f, 1.0f, 0.64f, 0.65f, 0.98f, 1.0f),
    colors2 = floatArrayOf(0.58f, 0.74f, 1.0f, 1.0f, 1.0f, 0.9f, 0.93f, 1.0f, 0.74f, 0.76f, 1.0f, 1.0f, 0.97f, 0.77f, 0.84f, 1.0f),
    colors3 = floatArrayOf(0.98f, 0.86f, 0.9f, 1.0f, 0.6f, 0.73f, 0.98f, 1.0f, 0.92f, 0.93f, 1.0f, 1.0f, 0.56f, 0.69f, 1.0f, 1.0f),
)

// BgEffectDataManager.mPhoneDarkData
private val PhoneDarkData = BgEffectData(
    points = floatArrayOf(0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f),
    alphaMulti = 1.0f,
    noiseScale = 1.5f,
    pointOffset = 0.4f,
    pointRadiusMulti = 1.0f,
    saturateOffset = 0.17f,
    lightOffset = 0.0f,
    alphaOffset = 0.5f,
    shadowColorMulti = 0.3f,
    shadowColorOffset = 0.3f,
    shadowNoiseScale = 5.0f,
    shadowOffset = 0.01f,
    translateY = 0.0f,
    colorInterpPeriod = 8.0f,
    gradientSpeedRest = 1.0f,
    colors1 = floatArrayOf(0.2f, 0.06f, 0.88f, 0.4f, 0.3f, 0.14f, 0.55f, 0.5f, 0.0f, 0.64f, 0.96f, 0.5f, 0.11f, 0.16f, 0.83f, 0.4f),
    colors2 = floatArrayOf(0.07f, 0.15f, 0.79f, 0.5f, 0.62f, 0.21f, 0.67f, 0.5f, 0.06f, 0.25f, 0.84f, 0.5f, 0.0f, 0.2f, 0.78f, 0.5f),
    colors3 = floatArrayOf(0.58f, 0.3f, 0.74f, 0.4f, 0.27f, 0.18f, 0.6f, 0.5f, 0.66f, 0.26f, 0.62f, 0.5f, 0.12f, 0.16f, 0.7f, 0.6f),
)

private fun easeOutCubic(t: Float): Float {
    val x = 1f - t
    return 1f - x * x * x
}

// Mirrors BgEffectPainter.computeGradientColor: a palette transition is triggered at
// every half period and settles quickly, then the palette holds until the next one.
private fun interpolatedColors(data: BgEffectData, time: Float): FloatArray {
    val phase = time / data.colorInterpPeriod * 2f
    val segment = floor(phase).toInt()
    val partial = phase - floor(phase)
    val halfPeriodSeconds = data.colorInterpPeriod / 2f
    val transition = easeOutCubic((partial * halfPeriodSeconds / 0.45f).coerceIn(0f, 1f))
    val (from, to) = when (((segment % 4) + 4) % 4) {
        0 -> data.colors2 to data.colors1
        1 -> data.colors1 to data.colors2
        2 -> data.colors2 to data.colors3
        else -> data.colors3 to data.colors2
    }
    return FloatArray(16) { i -> from[i] + (to[i] - from[i]) * transition }
}

private fun palette(colors: FloatArray): List<Color> = List(4) { i ->
    Color(
        red = colors[i * 4],
        green = colors[i * 4 + 1],
        blue = colors[i * 4 + 2],
        alpha = colors[i * 4 + 3],
    )
}

private fun loadShaderSource(context: Context): String =
    context.resources.openRawResource(R.raw.bg_frag).bufferedReader().use { it.readText() }

@Composable
internal fun AboutBackground(
    dark: Boolean,
    fade: Float,
    modifier: Modifier = Modifier,
) {
    val baseColor = if (dark) Color.Black else Color.White
    val alpha = fade.coerceIn(0f, 1f)

    Box(modifier = modifier.background(baseColor)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ShaderBackground(dark = dark, alpha = alpha, modifier = Modifier.fillMaxSize())
        } else {
            GradientFallback(dark = dark, alpha = alpha, modifier = Modifier.fillMaxSize())
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ShaderBackground(
    dark: Boolean,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val data = if (dark) PhoneDarkData else PhoneLightData
    // HyperCeiler limits uBound to the action bar + app_logo_area_height region. We use the
    // full viewport so the animated gradient sits behind every About card, not only the first
    // one; the scroll-driven fade still hands the page over to the solid base color.
    val shader = remember(context, dark) {
        runCatching {
            RuntimeShader(loadShaderSource(context)).also { applyStaticUniforms(it, data) }
        }.getOrNull()
    }
    val paint = remember(shader) {
        shader?.let { Paint().apply { isAntiAlias = false; this.shader = it } }
    }
    val time = remember(dark) { mutableFloatStateOf(0f) }
    val active = alpha > 0.005f && shader != null && paint != null

    LaunchedEffect(active, data) {
        if (!active) return@LaunchedEffect
        var last = 0L
        var accumulated = 0f
        var direction = 1f
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    val delta = ((now - last).coerceAtMost(100_000_000L)) / 1_000_000_000f
                    accumulated += delta
                    if (accumulated >= TargetFrameSeconds) {
                        var next = time.floatValue + accumulated * direction * data.gradientSpeedRest
                        if (direction > 0f && next >= PingPongSeconds) {
                            next = PingPongSeconds
                            direction = -1f
                        } else if (direction < 0f && next <= 0f) {
                            next = 0f
                            direction = 1f
                        }
                        time.floatValue = next
                        accumulated = 0f
                    }
                }
                last = now
            }
        }
    }

    if (shader == null || paint == null) {
        GradientFallback(dark = dark, alpha = alpha, modifier = modifier)
        return
    }
    val activeShader = shader
    val activePaint = paint

    modifier
        .drawBehind {
            if (size.width <= 0f || size.height <= 0f) return@drawBehind
            val current = time.floatValue
            val colors = interpolatedColors(data, current)
            activePaint.alpha = (alpha * 255f).roundToInt().coerceIn(0, 255)
            activeShader.setFloatUniform("uResolution", size.width, size.height)
            activeShader.setFloatUniform("uBound", 0f, 0f, 1f, 1f)
            activeShader.setFloatUniform("uAnimTime", current)
            activeShader.setFloatUniform("uColors", colors)
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, activePaint)
            }
        }
        .let { Box(Modifier.fillMaxSize().then(it)) }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun applyStaticUniforms(shader: RuntimeShader, data: BgEffectData) {
    shader.setFloatUniform("uTranslateY", data.translateY)
    shader.setFloatUniform("uPoints", data.points)
    shader.setFloatUniform("uColors", data.colors2)
    shader.setFloatUniform("uNoiseScale", data.noiseScale)
    shader.setFloatUniform("uPointOffset", data.pointOffset)
    shader.setFloatUniform("uPointRadiusMulti", data.pointRadiusMulti)
    shader.setFloatUniform("uSaturateOffset", data.saturateOffset)
    shader.setFloatUniform("uLightOffset", data.lightOffset)
    shader.setFloatUniform("uAlphaMulti", data.alphaMulti)
    shader.setFloatUniform("uAlphaOffset", data.alphaOffset)
    shader.setFloatUniform("uShadowColorMulti", data.shadowColorMulti)
    shader.setFloatUniform("uShadowColorOffset", data.shadowColorOffset)
    shader.setFloatUniform("uShadowNoiseScale", data.shadowNoiseScale)
    shader.setFloatUniform("uShadowOffset", data.shadowOffset)
}

@Composable
private fun GradientFallback(
    dark: Boolean,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val data = if (dark) PhoneDarkData else PhoneLightData
    val brush = remember(data) {
        Brush.verticalGradient(palette(data.colors2))
    }
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .background(brush),
    )
}
