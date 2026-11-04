/*
 * About page adapted from HyperCeiler
 * https://github.com/ReChronoRain/HyperCeiler
 * Copyright (C) 2023-2026 HyperCeiler Contributions
 * Licensed under the GNU Affero General Public License v3.0 (AGPL-3.0).
 *
 * Layout and behavior ported from HyperCeiler's AboutSettingsFragment / VersionCard
 * (View/XML) to Jetpack Compose. Colors, gradient palettes and animation timings are
 * taken from HyperCeiler's About resources:
 *  - BgEffectDataManager (gradientColors1/2, phone light & dark palettes)
 *  - values/colors.xml & values-night/colors.xml (app_version_text_color, card colors)
 *  - VersionCard.performLogoAnimation (800ms decelerate alpha, -30dp/1000ms ease-out rise)
 *  - AboutAnimationController (scroll fraction over app_logo_area_height = 389dp,
 *    scale = 1 - 0.1 * fraction, alpha = 1 - fraction)
 */
package com.wifirename.module

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val RepositoryUrl = "https://github.com/ItsCookie177/WiFiTitleRename"
private const val LicenseUrl = "https://github.com/ItsCookie177/WiFiTitleRename/blob/master/LICENSE"

private val HeroHeight = 389.dp

// HyperCeiler BgEffectDataManager, phone light palette (gradientColors1 -> gradientColors2)
private val LightPaletteA = listOf(
    Color(1.0f, 0.90f, 0.94f),
    Color(1.0f, 0.84f, 0.89f),
    Color(0.97f, 0.73f, 0.82f),
    Color(0.64f, 0.65f, 0.98f),
)
private val LightPaletteB = listOf(
    Color(0.58f, 0.74f, 1.0f),
    Color(1.0f, 0.90f, 0.93f),
    Color(0.74f, 0.76f, 1.0f),
    Color(0.97f, 0.77f, 0.84f),
)

// HyperCeiler BgEffectDataManager, phone dark palette (gradientColors1 -> gradientColors2)
private val DarkPaletteA = listOf(
    Color(0.20f, 0.06f, 0.88f, 0.40f),
    Color(0.30f, 0.14f, 0.55f, 0.50f),
    Color(0.00f, 0.64f, 0.96f, 0.50f),
    Color(0.11f, 0.16f, 0.83f, 0.40f),
)
private val DarkPaletteB = listOf(
    Color(0.07f, 0.15f, 0.79f, 0.50f),
    Color(0.62f, 0.21f, 0.67f, 0.50f),
    Color(0.06f, 0.25f, 0.84f, 0.50f),
    Color(0.00f, 0.20f, 0.78f, 0.50f),
)

// Title gradient sampled from HyperCeiler's About screenshot (dark); darker variant for light.
private val NameGradientDark = listOf(Color(0xFF5E82F5), Color(0xFF8C78EB))
private val NameGradientLight = listOf(Color(0xFF3F63D8), Color(0xFF6E4FD0))

// HyperCeiler app_version_text_color (#99ffffff dark, #99000000 light)
private val VersionTextDark = Color(0x99FFFFFF)
private val VersionTextLight = Color(0x99000000)

private data class LicenseEntry(
    val name: String,
    val license: String,
    val url: String,
    val compileOnly: Boolean = false,
)

private val ThirdPartyLicenses = listOf(
    LicenseEntry(
        name = "Miuix (UI, Preference, Icons)",
        license = "Apache-2.0",
        url = "https://github.com/compose-miuix-ui/miuix",
    ),
    LicenseEntry(
        name = "Compose Multiplatform",
        license = "Apache-2.0",
        url = "https://github.com/JetBrains/compose-multiplatform",
    ),
    LicenseEntry(
        name = "AndroidX (Activity, Core, Lifecycle)",
        license = "Apache-2.0",
        url = "https://developer.android.com/jetpack/androidx",
    ),
    LicenseEntry(
        name = "Kotlin Standard Library",
        license = "Apache-2.0",
        url = "https://kotlinlang.org",
    ),
    LicenseEntry(
        name = "Xposed Framework API",
        license = "Apache-2.0",
        url = "https://github.com/rovo89/Xposed",
        compileOnly = true,
    ),
    LicenseEntry(
        name = "HyperCeiler (About page adapted)",
        license = "AGPL-3.0",
        url = "https://github.com/ReChronoRain/HyperCeiler",
    ),
)

private fun openUrl(url: String, context: android.content.Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
    }
}

@Composable
fun AboutScreen(versionText: String, dark: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = true) { onBack() }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val heroPx = with(density) { HeroHeight.toPx() }
    val fraction = (scrollState.value / heroPx).coerceIn(0f, 1f)

    val titlesAlpha = ((fraction - 0.75f) / 0.25f).coerceIn(0f, 1f)
    val textColor = if (dark) Color.White else Color(0xFF15171C)
    val secondary = if (dark) Color.White.copy(alpha = 0.55f) else Color(0xFF15171C).copy(alpha = 0.55f)

    val transition = rememberInfiniteTransition(label = "about-bg")
    val blend by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "about-bg-blend",
    )
    val gradient = if (dark) {
        DarkPaletteA.indices.map { i -> lerp(DarkPaletteA[i], DarkPaletteB[i], blend) }
    } else {
        LightPaletteA.indices.map { i -> lerp(LightPaletteA[i], LightPaletteB[i], blend) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (dark) Color.Black else Color.White)
            .background(Brush.verticalGradient(gradient)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AboutHero(
                versionText = versionText,
                dark = dark,
                fraction = fraction,
                secondary = secondary,
            )

            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                insideMargin = PaddingValues(16.dp, 6.dp),
            ) {
                ArrowPreference(
                    title = stringResource(R.string.about_view_source),
                    onClick = { openUrl(RepositoryUrl, context) },
                    endActions = {
                        Text(text = "GitHub", fontSize = 15.sp)
                    },
                )
                ArrowPreference(
                    title = stringResource(R.string.about_license),
                    onClick = { openUrl(LicenseUrl, context) },
                    endActions = {
                        Text(text = "AGPL-3.0", fontSize = 15.sp)
                    },
                )
            }

            Text(
                text = stringResource(R.string.about_third_party_heading),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 8.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = secondary,
            )

            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                insideMargin = PaddingValues(0.dp),
            ) {
                ThirdPartyLicenses.forEachIndexed { index, entry ->
                    LicenseRow(entry = entry, secondary = secondary)
                    if (index != ThirdPartyLicenses.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            thickness = 0.75.dp,
                            color = MiuixTheme.colorScheme.dividerLine,
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.about_licenses_note),
                modifier = Modifier.padding(start = 36.dp, end = 36.dp, top = 12.dp),
                fontSize = 12.sp,
                color = secondary,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.about_source_notice),
                fontSize = 12.sp,
                color = secondary.copy(alpha = 0.75f),
            )
            Text(
                text = stringResource(R.string.about_ui_credit),
                modifier = Modifier.padding(top = 6.dp, bottom = 32.dp),
                fontSize = 12.sp,
                color = secondary.copy(alpha = 0.75f),
            )
        }

        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 6.dp)
                .align(Alignment.TopStart),
        ) {
            IconButton(
                onClick = onBack,
                minWidth = 48.dp,
                minHeight = 48.dp,
            ) {
                Icon(
                    imageVector = MiuixIcons.Back,
                    contentDescription = stringResource(R.string.back),
                    tint = textColor,
                )
            }
        }

        Text(
            text = stringResource(R.string.settings_about),
            modifier = Modifier
                .graphicsLayer { alpha = titlesAlpha }
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 12.dp)
                .align(Alignment.TopCenter),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

@Composable
private fun AboutHero(
    versionText: String,
    dark: Boolean,
    fraction: Float,
    secondary: Color,
) {
    val density = LocalDensity.current
    val alpha = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            alpha.animateTo(
                1f,
                tween(
                    durationMillis = 800,
                    easing = Easing { 1f - (1f - it) * (1f - it) },
                ),
            )
        }
        launch {
            delay(100)
            rise.animateTo(
                1f,
                tween(
                    durationMillis = 1000,
                    easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f),
                ),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HeroHeight)
            .windowInsetsPadding(WindowInsets.statusBars)
            .graphicsLayer {
                val scrollAlpha = 1f - fraction
                this.alpha = alpha.value * scrollAlpha
                val scale = 1f - 0.1f * fraction
                scaleX = scale
                scaleY = scale
                translationY = with(density) { 30.dp.toPx() } * (1f - rise.value)
            }
            .padding(top = 64.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF5E82F5).copy(alpha = if (dark) 0.30f else 0.20f),
                                Color(0xFF8C78EB).copy(alpha = if (dark) 0.10f else 0.08f),
                                Color.Transparent,
                            ),
                        ),
                        CircleShape,
                    ),
            )
            AppLogo()
        }
        GradientAppName(
            modifier = Modifier.padding(top = 16.dp),
            gradient = if (dark) NameGradientDark else NameGradientLight,
        )
        Text(
            text = versionText,
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 14.sp,
            color = if (dark) VersionTextDark else VersionTextLight,
        )
    }
}

@Composable
private fun LicenseRow(entry: LicenseEntry, secondary: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = entry.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entry.license,
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 12.sp,
                color = secondary,
            )
            if (entry.compileOnly) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "· " + stringResource(R.string.about_compile_only),
                    modifier = Modifier.padding(top = 2.dp),
                    fontSize = 12.sp,
                    color = secondary,
                )
            }
        }
        Text(
            text = entry.url,
            modifier = Modifier.padding(top = 2.dp),
            fontSize = 12.sp,
            color = secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AppLogo() {
    Box(
        modifier = Modifier
            .size(104.dp)
            .background(Color.White, RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(56.dp)) {
            val w = size.width
            val cx = w / 2f
            val cy = w / 2f
            val rOuter = w * 0.48f
            val rInner = w * 0.30f
            fun hexPoints(radius: Float): List<Offset> {
                val pts = mutableListOf<Offset>()
                for (i in 0 until 6) {
                    val angle = Math.toRadians(i * 60.0)
                    pts.add(
                        Offset(
                            (cx + radius * kotlin.math.cos(angle)).toFloat(),
                            (cy + radius * kotlin.math.sin(angle)).toFloat(),
                        ),
                    )
                }
                return pts
            }

            fun hexPath(points: List<Offset>): Path {
                val p = Path()
                p.moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) p.lineTo(points[i].x, points[i].y)
                p.close()
                return p
            }

            drawPath(
                path = hexPath(hexPoints(rOuter)),
                brush = Brush.verticalGradient(listOf(Color(0xFF5E8BF0), Color(0xFF7E6BE8))),
            )
            drawPath(path = hexPath(hexPoints(rInner)), color = Color.White)
            drawPath(
                path = hexPath(hexPoints(rInner * 0.62f)),
                brush = Brush.verticalGradient(listOf(Color(0xFF93BAEC), Color(0xFF7C9FE0))),
            )
        }
    }
}

@Composable
private fun GradientAppName(modifier: Modifier = Modifier, gradient: List<Color>) {
    androidx.compose.foundation.text.BasicText(
        text = "WiFi Title Rename",
        modifier = modifier,
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            brush = Brush.horizontalGradient(gradient),
            textAlign = TextAlign.Center,
        ),
        maxLines = 2,
        overflow = TextOverflow.Clip,
    )
}
