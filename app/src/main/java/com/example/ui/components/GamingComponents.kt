package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CrosshairConfig
import com.example.data.model.CrosshairStyle
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingGlassBg
import com.example.ui.theme.GamingPanelBg
import com.example.ui.theme.GamingSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SliderActive
import com.example.ui.theme.SliderTrack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun FuturisticHudGauge(
    ramPercent: Int,
    cpuPercent: Int,
    fps: Int,
    refreshRateHz: Float = 60f,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GamingPanelBg)
            .border(1.dp, GamingBorderGlow, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // RAM Arc Gauge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(68.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = SliderTrack,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = stroke
                    )
                    drawArc(
                        color = NeonGreen,
                        startAngle = 135f,
                        sweepAngle = 270f * (ramPercent.coerceIn(0, 100) / 100f),
                        useCenter = false,
                        style = stroke
                    )
                }
                Text(
                    text = "$ramPercent%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = "RAM",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center FPS Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(
                text = "$fps",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 32.sp
            )
            Text(
                text = "FPS • ${refreshRateHz.toInt()}Hz",
                color = NeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // CPU Arc Gauge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(68.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = SliderTrack,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = stroke
                    )
                    val sweep = if (cpuPercent >= 0) 270f * (cpuPercent.coerceIn(0, 100) / 100f) else 0f
                    drawArc(
                        color = if (cpuPercent >= 0) NeonCyan else Color.Gray,
                        startAngle = 135f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = stroke
                    )
                }
                Text(
                    text = if (cpuPercent >= 0) "$cpuPercent%" else "N/A",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = "CPU",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    title: String,
    isActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isActive) NeonGreen.copy(alpha = 0.25f) else GamingSurfaceVariant)
                .border(
                    width = 1.dp,
                    color = if (isActive) NeonGreen else GamingBorderGlow.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) NeonGreen else TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (isActive) NeonGreen else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp,
            modifier = Modifier.width(72.dp)
        )
    }
}

@Composable
fun GamingSliderWithSteps(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    step: Float = 0.05f,
    formatString: String = "%.2f",
    onValueChange: (Float) -> Unit,
    onReset: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format(formatString, value),
                    color = NeonGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (onReset != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RESET",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GamingSurfaceVariant)
                            .clickable(onClick = onReset)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    val nextVal = (value - step).coerceIn(min, max)
                    onValueChange(nextVal)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Kurang",
                    tint = NeonGreen
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = min..max,
                colors = SliderDefaults.colors(
                    thumbColor = NeonGreen,
                    activeTrackColor = SliderActive,
                    inactiveTrackColor = SliderTrack
                ),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    val nextVal = (value + step).coerceIn(min, max)
                    onValueChange(nextVal)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah",
                    tint = NeonGreen
                )
            }
        }
    }
}

@Composable
fun DpadPositionController(
    offsetX: Int,
    offsetY: Int,
    onStepChange: (dx: Int, dy: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(130.dp)
            .clip(CircleShape)
            .background(GamingSurfaceVariant)
            .border(1.dp, GamingBorderGlow, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // UP
        IconButton(
            onClick = { onStepChange(0, -1) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Up",
                tint = NeonGreen
            )
        }
        // DOWN
        IconButton(
            onClick = { onStepChange(0, 1) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Down",
                tint = NeonGreen
            )
        }
        // LEFT
        IconButton(
            onClick = { onStepChange(-1, 0) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Left",
                tint = NeonGreen
            )
        }
        // RIGHT
        IconButton(
            onClick = { onStepChange(1, 0) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Right",
                tint = NeonGreen
            )
        }

        // Center readout
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "X: $offsetX",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Y: $offsetY",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun CrosshairRenderer(
    config: CrosshairConfig,
    modifier: Modifier = Modifier
) {
    val color = try {
        Color(android.graphics.Color.parseColor(config.colorHex))
            .copy(alpha = (config.opacityPercent / 100f).coerceIn(0.1f, 1.0f))
    } catch (_: Exception) {
        NeonGreen
    }

    Box(
        modifier = modifier
            .offset { IntOffset(config.offsetX, config.offsetY) }
            .rotate(config.rotationDeg),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseSize = (size.minDimension / 2f) * (config.sizePercent / 100f)
            val strokePx = config.thicknessDp.dp.toPx()

            when (config.style) {
                CrosshairStyle.DOT -> {
                    drawCircle(
                        color = color,
                        radius = (baseSize / 3f).coerceAtLeast(strokePx),
                        center = center
                    )
                }
                CrosshairStyle.PLUS -> {
                    // Vertical
                    drawLine(
                        color = color,
                        start = Offset(center.x, center.y - baseSize),
                        end = Offset(center.x, center.y + baseSize),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                    // Horizontal
                    drawLine(
                        color = color,
                        start = Offset(center.x - baseSize, center.y),
                        end = Offset(center.x + baseSize, center.y),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                }
                CrosshairStyle.CROSS -> {
                    // Diagonal X
                    val diag = baseSize * 0.707f
                    drawLine(
                        color = color,
                        start = Offset(center.x - diag, center.y - diag),
                        end = Offset(center.x + diag, center.y + diag),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = color,
                        start = Offset(center.x - diag, center.y + diag),
                        end = Offset(center.x + diag, center.y - diag),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                }
                CrosshairStyle.CIRCLE -> {
                    drawCircle(
                        color = color,
                        radius = baseSize,
                        center = center,
                        style = Stroke(width = strokePx)
                    )
                    drawCircle(
                        color = color,
                        radius = (strokePx * 1.2f).coerceAtLeast(2f),
                        center = center
                    )
                }
                CrosshairStyle.TCROSS -> {
                    // T Shape (no top bar)
                    drawLine(
                        color = color,
                        start = Offset(center.x, center.y),
                        end = Offset(center.x, center.y + baseSize),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = color,
                        start = Offset(center.x - baseSize, center.y),
                        end = Offset(center.x + baseSize, center.y),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                }
                CrosshairStyle.DIAMOND -> {
                    val path = Path().apply {
                        moveTo(center.x, center.y - baseSize)
                        lineTo(center.x + baseSize, center.y)
                        lineTo(center.x, center.y + baseSize)
                        lineTo(center.x - baseSize, center.y)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = strokePx)
                    )
                    drawCircle(
                        color = color,
                        radius = strokePx,
                        center = center
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveTouchPad(
    modifier: Modifier = Modifier,
    sensitivityX: Float,
    sensitivityY: Float,
    acceleration: Boolean
) {
    val points = remember { mutableStateListOf<Offset>() }
    var currentSpeed by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GamingSurfaceVariant)
            .border(1.dp, GamingBorderGlow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        points.clear()
                        points.add(offset)
                    },
                    onDragEnd = {
                        points.clear()
                        currentSpeed = 0f
                    },
                    onDragCancel = {
                        points.clear()
                        currentSpeed = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val rawDx = dragAmount.x * sensitivityX
                        val rawDy = dragAmount.y * sensitivityY
                        val speedFactor = if (acceleration) 1.35f else 1.0f
                        currentSpeed = dragAmount.getDistance() * speedFactor

                        val last = points.lastOrNull() ?: change.position
                        val newPoint = Offset(
                            (last.x + rawDx * speedFactor).coerceIn(0f, size.width.toFloat()),
                            (last.y + rawDy * speedFactor).coerceIn(0f, size.height.toFloat())
                        )
                        if (points.size > 24) points.removeAt(0)
                        points.add(newPoint)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw grid lines
            val step = 30.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }

            // Draw swipe curve trail
            if (points.size > 1) {
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val alpha = (i.toFloat() / points.size).coerceIn(0.2f, 1.0f)
                    drawLine(
                        color = NeonGreen.copy(alpha = alpha),
                        start = p1,
                        end = p2,
                        strokeWidth = (i * 0.4f + 2f).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        if (points.isEmpty()) {
            Text(
                text = "Swipe here to test touch curve & response",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = "Velocity: ${(currentSpeed * 10).roundToInt()} px/s | Sensi: X ${String.format("%.2f", sensitivityX)} Y ${String.format("%.2f", sensitivityY)}",
                color = NeonCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}
