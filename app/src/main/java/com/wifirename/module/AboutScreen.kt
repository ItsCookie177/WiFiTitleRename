/*
 * About page adapted from HyperCeiler
 * https://github.com/ReChronoRain/HyperCeiler
 * Copyright (C) 2023-2026 HyperCeiler Contributions
 * Licensed under the GNU Affero General Public License v3.0 (AGPL-3.0).
 *
 * The hero card layout (logo + app name + version, entrance animation) and the
 * preference-group structure of the About screen are ported from HyperCeiler's
 * AboutSettingsFragment / VersionCard (View/XML) to Jetpack Compose.
 */
package com.wifirename.module

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference

private const val RepositoryUrl = "https://github.com/ItsCookie177/WiFiTitleRename"
private const val LicenseUrl = "https://www.gnu.org/licenses/agpl-3.0.txt"

private val GradientDark = listOf(Color(0xFF243456), Color(0xFF33305F), Color(0xFF41305B))
private val GradientLight = listOf(Color(0xFFD9E6F6), Color(0xFFE6E0F7), Color(0xFFF1E8F6))
private val NameGradientDark = listOf(Color(0xFF7FB4FF), Color(0xFFC3A5FF))
private val NameGradientLight = listOf(Color(0xFF2F6BDF), Color(0xFF7A45D6))

private fun openUrl(url: String, context: android.content.Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
    }
}

data class LicenseEntry(val name: String, val license: String)

private val ThirdPartyLicenses = listOf(
    LicenseEntry("HyperCeiler (About page adapted)", "AGPL-3.0"),
    LicenseEntry("Miuix (top.yukonga.miuix.kmp)", "Apache-2.0"),
    LicenseEntry("Jetpack Compose / Compose Multiplatform", "Apache-2.0"),
    LicenseEntry("AndroidX Activity-Compose", "Apache-2.0"),
    LicenseEntry("Kotlin (kotlin-stdlib)", "Apache-2.0"),
    LicenseEntry("Xposed Framework API (de.robv.android.xposed:api:82)", "Apache-2.0"),
)

@Composable
fun AboutScreen(versionText: String, dark: Boolean, onBack: () -> Unit) {
    var licensesOpen by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (licensesOpen) licensesOpen = false else onBack()
    }

    AnimatedContent(
        targetState = licensesOpen,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(tween(220)) { it / 3 } + fadeIn(tween(220))) togetherWith
                    (slideOutHorizontally(tween(220)) { -it / 6 } + fadeOut(tween(200)))
            } else {
                (slideInHorizontally(tween(220)) { -it / 6 } + fadeIn(tween(220))) togetherWith
                    (slideOutHorizontally(tween(220)) { it / 3 } + fadeOut(tween(200)))
            }
        },
        label = "about",
    ) { showLicenses ->
        if (showLicenses) {
            LicensesPage(dark = dark, onBack = { licensesOpen = false })
        } else {
            AboutMain(versionText = versionText, dark = dark, onBack = onBack) { licensesOpen = true }
        }
    }
}

private data class AboutColors(
    val gradient: List<Color>,
    val text: Color,
    val secondary: Color,
    val card: Color,
    val onCard: Color,
    val nameGradient: List<Color>,
)

@Composable
private fun aboutColors(dark: Boolean) = if (dark) {
    AboutColors(
        gradient = GradientDark,
        text = Color.White,
        secondary = Color.White.copy(alpha = 0.55f),
        card = Color.White.copy(alpha = 0.10f),
        onCard = Color.White,
        nameGradient = NameGradientDark,
    )
} else {
    AboutColors(
        gradient = GradientLight,
        text = Color(0xFF15171C),
        secondary = Color(0xFF15171C).copy(alpha = 0.55f),
        card = Color.White.copy(alpha = 0.72f),
        onCard = Color(0xFF15171C),
        nameGradient = NameGradientLight,
    )
}

@Composable
private fun AboutLayout(
    dark: Boolean,
    scrolledTitle: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = aboutColors(dark)
    val scrollState = rememberScrollState()
    val titleAlpha: Float = if (scrollState.value > 420) 1f else 0f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors.gradient)),
    ) {
        content()
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 6.dp)
                .align(Alignment.TopStart),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = MiuixIcons.Back,
                    contentDescription = "Kembali",
                    tint = colors.text,
                )
            }
        }
        Text(
            text = scrolledTitle,
            modifier = Modifier
                .graphicsLayer { alpha = titleAlpha }
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 10.dp)
                .align(Alignment.TopCenter),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text,
        )
    }
}

@Composable
private fun AboutMain(versionText: String, dark: Boolean, onBack: () -> Unit, onOpenLicenses: () -> Unit) {
    val context = LocalContext.current
    val colors = aboutColors(dark)
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 360, easing = FastOutSlowInEasing))
    }

    AboutLayout(dark = dark, scrolledTitle = "Tentang", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 64.dp)
                    .graphicsLayer {
                        val p = enter.value
                        alpha = p
                        val scale = 0.94f + 0.06f * p
                        scaleX = scale
                        scaleY = scale
                        translationY = (1f - p) * 40f
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        colors.nameGradient.first().copy(alpha = 0.30f),
                                        colors.nameGradient.last().copy(alpha = 0.10f),
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
                    gradient = colors.nameGradient,
                )
                Text(
                    text = versionText,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 14.sp,
                    color = colors.secondary,
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                insideMargin = androidx.compose.foundation.layout.PaddingValues(16.dp, 6.dp),
                colors = CardDefaults.defaultColors(
                    color = colors.card,
                    contentColor = colors.onCard,
                ),
            ) {
                ArrowPreference(
                    title = "View Source",
                    onClick = { openUrl(RepositoryUrl, context) },
                    endActions = {
                        Text(
                            text = "GitHub",
                            fontSize = 15.sp,
                            color = colors.secondary,
                        )
                    },
                )
                ArrowPreference(
                    title = "License",
                    onClick = { openUrl(LicenseUrl, context) },
                    endActions = {
                        Text(
                            text = "AGPL-3.0",
                            fontSize = 15.sp,
                            color = colors.secondary,
                        )
                    },
                )
                ArrowPreference(
                    title = "Third Party Licenses",
                    onClick = onOpenLicenses,
                )
            }

            Text(
                text = "Source must remain public · AGPL-3.0",
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 12.sp,
                color = colors.secondary.copy(alpha = 0.75f),
            )
            Text(
                text = "UI by Miuix",
                modifier = Modifier.padding(top = 6.dp, bottom = 32.dp),
                fontSize = 12.sp,
                color = colors.secondary.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun LicensesPage(dark: Boolean, onBack: () -> Unit) {
    val colors = aboutColors(dark)
    AboutLayout(dark = dark, scrolledTitle = "Third Party Licenses", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 64.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                insideMargin = androidx.compose.foundation.layout.PaddingValues(16.dp, 6.dp),
                colors = CardDefaults.defaultColors(
                    color = colors.card,
                    contentColor = colors.onCard,
                ),
            ) {
                ThirdPartyLicenses.forEach { entry ->
                    ArrowPreference(
                        title = entry.name,
                        summary = entry.license,
                    )
                }
            }
            Text(
                text = "Setiap library dilisensikan oleh pemiliknya masing-masing.",
                modifier = Modifier.padding(horizontal = 36.dp),
                fontSize = 12.sp,
                color = colors.secondary,
            )
        }
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
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
