package com.wifirename.module

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference

private val AboutGradientTop = Color(0xFF2F5F8E)
private val AboutGradientBottom = Color(0xFF34275C)
private val NameGradientStart = Color(0xFF5AA8F5)
private val NameGradientEnd = Color(0xFFA578F0)

private const val RepositoryUrl = "https://github.com/ItsCookie177/WiFiTitleRename"

private fun openUrl(url: String, context: android.content.Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
    }
}

data class LicenseEntry(val name: String, val license: String)

private val ThirdPartyLicenses = listOf(
    LicenseEntry("Miuix (top.yukonga.miuix.kmp:miuix-ui, miuix-preference)", "Apache-2.0"),
    LicenseEntry("Jetpack Compose / Compose Multiplatform (org.jetbrains.compose.*)", "Apache-2.0"),
    LicenseEntry("AndroidX Activity-Compose (androidx.activity:activity-compose)", "Apache-2.0"),
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
private fun AboutLayout(
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AboutGradientTop, AboutGradientBottom),
                ),
            ),
    ) {
        content()
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 6.dp, top = 4.dp)
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
    }
}

@Composable
private fun AboutMain(versionText: String, onBack: () -> Unit, onOpenLicenses: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    AboutLayout(onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppLogo()
            Column(
                modifier = Modifier.padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GradientAppName()
                Text(
                    text = versionText,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }

            Card(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 96.dp)
                    .fillMaxWidth(),
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = Color.White.copy(alpha = 0.10f),
                    contentColor = Color.White,
                ),
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
            }

            Card(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp)
                    .fillMaxWidth(),
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = Color.White.copy(alpha = 0.10f),
                    contentColor = Color.White,
                ),
            ) {
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

            Text(
                text = "UI by Miuix",
                modifier = Modifier.padding(top = 24.dp),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.40f),
            )
        }
    }
}

@Composable
private fun LicensesPage(onBack: () -> Unit) {
    AboutLayout(onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 56.dp, bottom = 32.dp),
        ) {
            Text(
                text = "Third Party Licenses",
                modifier = Modifier.padding(start = 40.dp, bottom = 16.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = Color.White.copy(alpha = 0.10f),
                    contentColor = Color.White,
                ),
            ) {
                ThirdPartyLicenses.forEach { entry ->
                    ArrowPreference(
                        title = entry.name,
                        summary = entry.license,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppLogo() {
    Box(
        modifier = Modifier
            .size(112.dp)
            .background(Color.White, RoundedCornerShape(28.dp)),
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
                brush = Brush.verticalGradient(listOf(Color(0xFF3B78D8), Color(0xFF2453B8))),
            )
            drawPath(
                path = hexPath(hexPathPoints(rInner)),
                color = Color.White,
            )
            drawPath(
                path = hexPath(hexPathPoints(rInner * 0.62f)),
                brush = Brush.verticalGradient(listOf(Color(0xFF8FB8E8), Color(0xFF6E9AD6))),
            )
        }
    }
}

@Composable
private fun GradientAppName() {
    val brush = Brush.horizontalGradient(listOf(NameGradientStart, NameGradientEnd))
    androidx.compose.foundation.text.BasicText(
        text = "WiFi Title Rename",
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            brush = brush,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
