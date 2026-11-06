package com.wifirename.module

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.drawCheckerboard
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = MiuixTheme.colorScheme.background.luminance() < 0.5f
    val container = MiuixTheme.colorScheme.surfaceContainerHigh
    val selected = if (dark) Color.Black.copy(alpha = 0.32f) else Color.Black.copy(alpha = 0.08f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .then(if (isSelected) Modifier.background(selected) else Modifier)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelected(index) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MiuixTheme.colorScheme.onSurface
                    } else {
                        MiuixTheme.colorScheme.onSurfaceVariantSummary
                    },
                )
            }
        }
    }
}

@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    checkerboard: Boolean = false,
) {
    val density = LocalDensity.current
    val trackHeight = 28.dp
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    var widthPx by remember { mutableFloatStateOf(0f) }
    var dragX by remember { mutableFloatStateOf(0f) }

    fun valueFor(x: Float): Float {
        if (widthPx <= 0f) return 0f
        val usable = (widthPx - trackHeightPx).coerceAtLeast(1f)
        return ((x - trackHeightPx / 2f) / usable).coerceIn(0f, 1f)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(CircleShape)
            .then(if (checkerboard) Modifier.drawCheckerboard() else Modifier)
            .drawWithCache {
                val half = trackHeightPx / 2f
                val brush = Brush.horizontalGradient(
                    colors = colors,
                    startX = half,
                    endX = size.width - half,
                    tileMode = TileMode.Clamp,
                )
                onDrawBehind {
                    drawRect(brush = brush)
                    drawRect(
                        color = Color.Black.copy(alpha = 0.12f),
                        style = Stroke(width = 0.5.dp.toPx()),
                    )
                }
            }
            .onSizeChanged { widthPx = it.width.toFloat() }
            .pointerInput(widthPx) {
                detectTapGestures { offset -> currentOnValueChange(valueFor(offset.x)) }
            }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    dragX = (dragX + delta).coerceIn(0f, widthPx)
                    currentOnValueChange(valueFor(dragX))
                },
                onDragStarted = { offset ->
                    dragX = offset.x
                    currentOnValueChange(valueFor(offset.x))
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    val usable = (widthPx - trackHeightPx).coerceAtLeast(0f)
                    IntOffset((value.coerceIn(0f, 1f) * usable).roundToInt(), 0)
                }
                .size(trackHeight)
                .drawWithCache {
                    val stroke = 5.dp.toPx()
                    val glow = 2.dp.toPx()
                    val radius = (size.minDimension - stroke) / 2f
                    val glowRadius = radius + stroke / 2f + glow
                    val glowBrush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.30f),
                            Color.Black.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                        radius = glowRadius,
                    )
                    onDrawBehind {
                        drawCircle(brush = glowBrush, radius = glowRadius)
                        drawCircle(
                            color = Color.White,
                            radius = radius,
                            style = Stroke(width = stroke),
                        )
                    }
                },
        )
    }
}

@Composable
fun PaletteGrid(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = 7
    val hueColumns = 12
    val totalColumns = hueColumns + 1
    val lightnessRows = remember { listOf(0.88f, 0.76f, 0.64f, 0.52f, 0.42f, 0.32f, 0.22f) }
    val saturationRows = remember { listOf(0.55f, 0.70f, 0.85f, 0.95f, 1f, 1f, 1f) }
    val hsl = color.toHsl()
    val markerColumn = if (hsl.s < 0.08f) {
        hueColumns
    } else {
        (hsl.h / 30f).toInt().coerceIn(0, hueColumns - 1)
    }
    val markerRow = lightnessRows.indices.minByOrNull { kotlin.math.abs(lightnessRows[it] - hsl.l) } ?: 0
    var bounds by remember { mutableStateOf(IntSize.Zero) }
    val currentOnColorChange by rememberUpdatedState(onColorChange)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .onSizeChanged { bounds = it }
            .pointerInput(bounds) {
                detectTapGestures { offset ->
                    if (bounds.width == 0 || bounds.height == 0) return@detectTapGestures
                    val cellWidth = bounds.width.toFloat() / totalColumns
                    val cellHeight = bounds.height.toFloat() / rows
                    val column = (offset.x / cellWidth).toInt().coerceIn(0, totalColumns - 1)
                    val row = (offset.y / cellHeight).toInt().coerceIn(0, rows - 1)
                    val base = if (column == hueColumns) {
                        hslColor(0f, 0f, lightnessRows[row])
                    } else {
                        hslColor(column * 30f, saturationRows[row], lightnessRows[row])
                    }
                    currentOnColorChange(base.copy(alpha = color.alpha))
                }
            },
    ) {
        val cellWidth = size.width / totalColumns
        val cellHeight = size.height / rows
        for (row in 0 until rows) {
            for (column in 0 until totalColumns) {
                val cellColor = if (column == hueColumns) {
                    hslColor(0f, 0f, lightnessRows[row])
                } else {
                    hslColor(column * 30f, saturationRows[row], lightnessRows[row])
                }
                drawRect(
                    color = cellColor,
                    topLeft = Offset(column * cellWidth, row * cellHeight),
                    size = Size(cellWidth + 0.5f, cellHeight + 0.5f),
                )
            }
        }
        val centerX = markerColumn * cellWidth + cellWidth / 2f
        val centerY = markerRow * cellHeight + cellHeight / 2f
        val radius = min(cellWidth, cellHeight) * 0.36f
        drawCircle(
            color = Color.Black.copy(alpha = 0.28f),
            radius = radius + 1.dp.toPx(),
            center = Offset(centerX, centerY),
        )
        drawCircle(
            color = Color.White,
            radius = radius,
            center = Offset(centerX, centerY),
        )
        drawCircle(
            color = color.copy(alpha = 1f),
            radius = radius * 0.45f,
            center = Offset(centerX, centerY),
        )
    }
}

@Composable
fun ColorSwatch(
    color: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .drawCheckerboard()
            .background(color)
            .border(0.5.dp, MiuixTheme.colorScheme.outline, shape),
    )
}

@Composable
fun ColorValueSlider(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    checkerboard: Boolean = false,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            Text(
                text = valueText,
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        GradientSlider(
            value = value,
            onValueChange = onValueChange,
            colors = colors,
            checkerboard = checkerboard,
        )
    }
}

@Composable
fun PercentValue(value: Float): String =
    stringResource(R.string.icon_color_percent_value, (value.coerceIn(0f, 1f) * 100f).roundToInt())

@Composable
fun DegreesValue(value: Float): String =
    stringResource(R.string.icon_color_hue_value, value.roundToInt())
