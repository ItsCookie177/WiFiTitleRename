package com.wifirename.module

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.alpha
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

private val AboutGradientTop = Color(0xFF2C3F66)
private val AboutGradientMid = Color(0xFF3A3A72)
private val AboutGradientBottom = Color(0xFF463263)
private val NameGradientStart = Color(0xFF66A9F8)
private val NameGradientEnd = Color(0xFFA887F5)

private const val RepositoryUrl = "https://github.com/ItsCookie177/WiFiTitleRename"

private fun openUrl(url: String, context: android.content.Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
    }
}

data class LicenseEntry(val name: String, val license: String)

private val ThirdPartyLicenses = listOf(
    LicenseEntry("Miuix (top.yukonga.miuix.kmp:miuix-ui)", "Apache-2.0"),
    LicenseEntry("Miuix Preference & Icons (top.yukonga.miuix.kmp)", "Apache-2.0"),
    LicenseEntry("Jetpack Compose / Compose Multiplatform", "Apache-2.0"),
    LicenseEntry("AndroidX Activity-Compose", "Apache-2.0"),
    LicenseEntry("Kotlin (kotlin-stdlib)", "Apache-2.0"),
    LicenseEntry("Xposed Framework API (de.robv.android.xposed:api:82)", "Apache-2.0"),
)

@Composable
fun AboutScreen(versionText: String, onBack: () -> Unit) {
    var licensesOpen by rememberSaveable { mutableStateOf(false) }

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
            LicensesPage(onBack = { licensesOpen = false })
        } else {
            AboutMain(versionText = versionText, onBack = onBack) { licensesOpen = true }
        }
    }
}

@Composable
private fun aboutCardColors() = CardDefaults.defaultColors(
    color = Color.White.copy(alpha = 0.10f),
    contentColor = Color.White,
)

@Composable
private fun AboutLayout(
    onBack: () -> Unit,
    scrolledTitle: String,
    content: @Composable (Modifier) -> Unit,
) {
    val scrollState = rememberScrollState()
    val titleAlpha: Float = if (scrollState.value > 420) 1f else 0f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AboutGradientTop, AboutGradientMid, AboutGradientBottom),
                ),
            ),
    ) {
        content(Modifier)
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
                    tint = Color.White,
                )
            }
        }
        Text(
            text = scrolledTitle,
            modifier = Modifier
                .alpha(titleAlpha)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 10.dp)
                .align(Alignment.TopCenter),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

@Composable
private fun AboutMain(versionText: String, onBack: () -> Unit, onOpenLicenses: () -> Unit) {
    val context = LocalContext.current
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 320, easing = FastOutSlowInEasing))
    }
    AboutLayout(
        onBack = onBack,
        scrolledTitle = "Tentang",
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = enter.value
                        translationY = (1f - enter.value) * 56f
                    }
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 68.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppLogo()
                GradientAppName(modifier = Modifier.padding(top = 20.dp))
                Text(
                    text = versionText,
                    modifier = Modifier.padding(top = 10.dp),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 72.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    colors = aboutCardColors(),
                ) {
                    ArrowPreference(
                        title = "View Source",
                        onClick = { openUrl(RepositoryUrl, context) },
                        endActions = {
                            Text(
                                text = "GitHub",
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.55f),
                            )
                        },
                    )
                    ArrowPreference(
                        title = "License",
                        onClick = { openUrl("https://www.apache.org/licenses/LICENSE-2.0.txt", context) },
                        endActions = {
                            Text(
                                text = "Apache-2.0",
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.55f),
                            )
                        },
                    )
                    ArrowPreference(
                        title = "Third Party Licenses",
                        onClick = onOpenLicenses,
                    )
                }
            }

            Text(
                text = "UI by Miuix",
                modifier = Modifier.padding(top = 24.dp, bottom = 32.dp),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.40f),
            )
        }
    }
}

@Composable
private fun LicensesPage(onBack: () -> Unit) {
    AboutLayout(onBack = onBack, scrolledTitle = "Third Party Licenses") { _ ->
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
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                colors = aboutCardColors(),
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
                modifier = Modifier.padding(horizontal = 40.dp),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.40f),
            )
        }
    }
}

@Composable
private fun AppLogo() {
    Box(
        modifier = Modifier
            .size(112.dp)
            .background(Color.White, RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(60.dp)) {
            val w = size.width
            val cx = w / 2f
            val cy = w / 2f
            val rOuter = w * 0.48f
            val rInner = w * 0.30f
            fun hexPathPoints(radius: Float): List<Offset> {
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
                path = hexPath(hexPathPoints(rOuter)),
                brush = Brush.verticalGradient(listOf(Color(0xFF5E8BF0), Color(0xFF7E6BE8))),
            )
            drawPath(
                path = hexPath(hexPathPoints(rInner)),
                color = Color.White,
            )
            drawPath(
                path = hexPath(hexPathPoints(rInner * 0.62f)),
                brush = Brush.verticalGradient(listOf(Color(0xFF93BAEC), Color(0xFF7C9FE0))),
            )
        }
    }
}

@Composable
private fun GradientAppName(modifier: Modifier = Modifier) {
    val brush = Brush.horizontalGradient(listOf(NameGradientStart, NameGradientEnd))
    androidx.compose.foundation.text.BasicText(
        text = "WiFi Title Rename",
        modifier = modifier,
        style = TextStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            brush = brush,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
