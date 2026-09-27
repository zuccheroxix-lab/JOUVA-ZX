package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TelemetryData
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingPanelBg
import com.example.ui.theme.GamingSurface
import com.example.ui.theme.GamingSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SliderActive
import com.example.ui.theme.SliderTrack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class MetricsOverlayMode {
    COMPACT_PILL,
    EXPANDED_CARD
}

/**
 * Service-based Compose overlay component that displays real-time frame rate (FPS)
 * and CPU/GPU usage metrics over apps and games.
 */
@Composable
fun RealtimeMetricsOverlayComponent(
    telemetry: TelemetryData,
    modifier: Modifier = Modifier,
    initialMode: MetricsOverlayMode = MetricsOverlayMode.COMPACT_PILL,
    initialAlpha: Float = 0.92f,
    onClose: (() -> Unit)? = null,
    onDragDelta: ((dx: Float, dy: Float) -> Unit)? = null
) {
    var mode by remember { mutableStateOf(initialMode) }
    var overlayAlpha by remember { mutableFloatStateOf(initialAlpha) }

    val fpsColor = when {
        telemetry.fps >= 55 -> NeonGreen
        telemetry.fps >= 40 -> NeonCyan
        telemetry.fps >= 25 -> NeonYellow
        else -> NeonRed
    }

    val cpuColor = when {
        telemetry.cpuUsagePercent >= 85 -> NeonRed
        telemetry.cpuUsagePercent >= 65 -> NeonYellow
        else -> NeonCyan
    }

    val gpuColor = when {
        telemetry.gpuUsagePercent >= 85 -> Color(0xFFFF5252)
        telemetry.gpuUsagePercent >= 65 -> Color(0xFFFFAB40)
        else -> Color(0xFFE040FB)
    }

    val dragModifier = if (onDragDelta != null) {
        Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                onDragDelta(dragAmount.x, dragAmount.y)
            }
        }
    } else Modifier

    Box(
        modifier = modifier
            .alpha(overlayAlpha)
            .animateContentSize()
            .then(dragModifier)
            .testTag("realtime_metrics_overlay_root")
    ) {
        if (mode == MetricsOverlayMode.COMPACT_PILL) {
            CompactMetricsPill(
                telemetry = telemetry,
                fpsColor = fpsColor,
                cpuColor = cpuColor,
                gpuColor = gpuColor,
                onExpand = { mode = MetricsOverlayMode.EXPANDED_CARD },
                onClose = onClose
            )
        } else {
            ExpandedMetricsCard(
                telemetry = telemetry,
                fpsColor = fpsColor,
                cpuColor = cpuColor,
                gpuColor = gpuColor,
                overlayAlpha = overlayAlpha,
                onAlphaChange = { overlayAlpha = it },
                onCollapse = { mode = MetricsOverlayMode.COMPACT_PILL },
                onClose = onClose
            )
        }
    }
}

@Composable
fun CompactMetricsPill(
    telemetry: TelemetryData,
    fpsColor: Color,
    cpuColor: Color,
    gpuColor: Color,
    onExpand: () -> Unit,
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xE60D151D),
                        Color(0xF0080E13)
                    )
                )
            )
            .border(1.5.dp, GamingBorderGlow.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .clickable(onClick = onExpand)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("metrics_compact_pill")
    ) {
        // Drag icon hint
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Move HUD",
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
        )

        // FPS Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(fpsColor.copy(alpha = 0.15f))
                .border(1.dp, fpsColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fpsColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${telemetry.fps}",
                color = fpsColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "FPS",
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${String.format(java.util.Locale.US, "%.0f", telemetry.refreshRateHz)}Hz",
                color = NeonCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (telemetry.frameTimeMs > 0f) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "(${String.format(java.util.Locale.US, "%.1f", telemetry.frameTimeMs)}ms)",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // CPU Metric
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(cpuColor.copy(alpha = 0.12f))
                .border(1.dp, cpuColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "CPU",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (telemetry.cpuUsagePercent >= 0) "${telemetry.cpuUsagePercent}%" else "--%",
                color = cpuColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // GPU Metric
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(gpuColor.copy(alpha = 0.12f))
                .border(1.dp, gpuColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "GPU",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (telemetry.gpuUsagePercent >= 0) "${telemetry.gpuUsagePercent}%" else "--%",
                color = gpuColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Expand action
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(GamingSurfaceVariant)
                .clickable(onClick = onExpand)
                .testTag("expand_metrics_btn")
        ) {
            Icon(
                imageVector = Icons.Default.OpenInFull,
                contentDescription = "Expand HUD",
                tint = NeonGreen,
                modifier = Modifier.size(12.dp)
            )
        }

        // Optional close action
        if (onClose != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FF3366))
                    .clickable(onClick = onClose)
                    .testTag("close_compact_metrics_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close HUD",
                    tint = NeonRed,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun ExpandedMetricsCard(
    telemetry: TelemetryData,
    fpsColor: Color,
    cpuColor: Color,
    gpuColor: Color,
    overlayAlpha: Float,
    onAlphaChange: (Float) -> Unit,
    onCollapse: () -> Unit,
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(320.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GamingPanelBg.copy(alpha = 0.95f),
                        Color(0xF5060A0D)
                    )
                )
            )
            .border(1.5.dp, GamingBorderGlow, RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("metrics_expanded_card")
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REAL-TIME TELEMETRY",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Collapse Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(GamingSurfaceVariant)
                        .clickable(onClick = onCollapse)
                        .testTag("collapse_metrics_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.UnfoldLess,
                        contentDescription = "Collapse HUD",
                        tint = NeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (onClose != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FF3366))
                            .clickable(onClick = onClose)
                            .testTag("close_expanded_metrics_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close HUD",
                            tint = NeonRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Three Main Diagnostic Gauges: FPS | CPU | GPU
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // FPS Box
            MetricBox(
                modifier = Modifier.weight(1f),
                title = "FPS (LIVE)",
                value = "${telemetry.fps}",
                subLabel = "${String.format(java.util.Locale.US, "%.1f", telemetry.refreshRateHz)}Hz (${telemetry.frameTimeMs}ms)",
                accentColor = fpsColor
            )

            // CPU Box
            MetricBox(
                modifier = Modifier.weight(1f),
                title = "CPU",
                value = if (telemetry.cpuUsagePercent >= 0) "${telemetry.cpuUsagePercent}%" else "--%",
                subLabel = "Utilized",
                accentColor = cpuColor
            )

            // GPU Box
            MetricBox(
                modifier = Modifier.weight(1f),
                title = "GPU",
                value = if (telemetry.gpuUsagePercent >= 0) "${telemetry.gpuUsagePercent}%" else "--%",
                subLabel = if (telemetry.gpuFrequencyMhz > 0) "${telemetry.gpuFrequencyMhz}MHz" else "Pipeline",
                accentColor = gpuColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Real-time Choreographer VSYNC Diagnostics Strip & Sparkline
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF04080D))
                .border(1.dp, GamingBorderGlow.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHOREOGRAPHER VSYNC",
                    color = NeonGreen,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "1% Low: ${telemetry.fpsOnePercentLow} FPS",
                    color = if (telemetry.fpsOnePercentLow < 40) NeonYellow else NeonCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Jank: ${telemetry.jankCount}",
                    color = if (telemetry.jankCount > 0) NeonYellow else TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (telemetry.fpsHistory.size >= 2) {
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF070B0E))
                ) {
                    val points = telemetry.fpsHistory
                    val maxVal = (telemetry.refreshRateHz.coerceAtLeast(60f) * 1.15f)
                    val stepX = size.width / (points.size - 1)
                    val path = Path()
                    points.forEachIndexed { i, fpsVal ->
                        val x = i * stepX
                        val y = size.height - ((fpsVal / maxVal).coerceIn(0f, 1f) * size.height)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = fpsColor,
                        style = Stroke(width = 1.8f.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bars for CPU & GPU
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GamingSurface)
                .padding(10.dp)
        ) {
            // CPU progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "CPU Load", color = TextSecondary, fontSize = 10.sp)
                Text(
                    text = if (telemetry.cpuUsagePercent >= 0) "${telemetry.cpuUsagePercent}%" else "Unavailable",
                    color = cpuColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = {
                    if (telemetry.cpuUsagePercent >= 0) (telemetry.cpuUsagePercent / 100f).coerceIn(0f, 1f) else 0.35f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = cpuColor,
                trackColor = GamingSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GPU progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "GPU Load (${telemetry.gpuRenderer})", color = TextSecondary, fontSize = 10.sp)
                Text(
                    text = if (telemetry.gpuUsagePercent >= 0) "${telemetry.gpuUsagePercent}%" else "Unavailable",
                    color = gpuColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = {
                    if (telemetry.gpuUsagePercent >= 0) (telemetry.gpuUsagePercent / 100f).coerceIn(0f, 1f) else 0.40f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = gpuColor,
                trackColor = GamingSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Info Bar: RAM & Temp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "RAM: ${telemetry.ramPercent}%",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = if (telemetry.batteryTemperatureC > 42f) NeonRed else NeonYellow,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (telemetry.batteryTemperatureC > 0) "${String.format("%.1f", telemetry.batteryTemperatureC)}°C" else "Norm",
                    color = if (telemetry.batteryTemperatureC > 42f) NeonRed else NeonYellow,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = telemetry.currentTimeString,
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transparency Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Opacity",
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.width(50.dp)
            )
            Slider(
                value = overlayAlpha,
                onValueChange = onAlphaChange,
                valueRange = 0.35f..1.0f,
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                colors = SliderDefaults.colors(
                    thumbColor = NeonGreen,
                    activeTrackColor = SliderActive,
                    inactiveTrackColor = SliderTrack
                )
            )
            Text(
                text = "${(overlayAlpha * 100).toInt()}%",
                color = NeonGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    subLabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GamingSurface)
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = accentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subLabel,
            color = TextMuted,
            fontSize = 8.sp,
            maxLines = 1
        )
    }
}
